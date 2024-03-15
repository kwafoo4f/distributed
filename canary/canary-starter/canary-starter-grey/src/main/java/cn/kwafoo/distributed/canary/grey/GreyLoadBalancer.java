package cn.kwafoo.distributed.canary.grey;

import cn.kwafoo.common.constant.Headers;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.balancer.NacosBalancer;
import com.alibaba.nacos.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 灰度发布,配合网关的GreyFilter使用
 */
@Slf4j
public class GreyLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final String serviceId;

    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    public GreyLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId, NacosDiscoveryProperties nacosDiscoveryProperties) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map(serviceInstances -> getInstanceResponse(serviceInstances, request));
    }

    /**
     * 优先获取与本地IP一致的服务，否则获取同一集群服务
     *
     * @param serviceInstances
     * @return
     */
    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> serviceInstances, Request request) {
        try {
            if (serviceInstances.isEmpty()) {
                log.warn("No servers available for service: " + this.serviceId);
                return new EmptyResponse();
            }
            // 获得 HttpHeaders 属性，实现从 header 中获取 version
            String version = Optional.ofNullable(request).map(Request::getContext).map(context->(RequestDataContext)context)
                    .map(RequestDataContext::getClientRequest).map(RequestData::getHeaders)
                    .map(httpHeaders -> httpHeaders.getFirst(Headers.VERSION)).orElse("");

            // 灰度发布,使用请求头中的version判断
            if (StringUtils.isNotBlank(version)) {
                return this.getGreyInstanceResponse(serviceInstances,version);
            }
            return this.getClusterInstanceResponse(serviceInstances);
        } catch (Exception e) {
            log.warn("getInstanceResponse error", e);
            return new EmptyResponse();
        }
    }

    /**
     * 获取灰度发布的实例
     *
     * @param serviceInstances
     * @return
     */
    private Response<ServiceInstance> getGreyInstanceResponse(List<ServiceInstance> serviceInstances,String version) {
        if (serviceInstances.isEmpty()) {
            log.warn("GreyBalancer:No servers available for service: {}" ,this.serviceId);
            return new EmptyResponse();
        }

        try {
            List<ServiceInstance> instancesToChoose = serviceInstances;
            // 匹配灰度节点:获取该节点在nacos的MeteDate配置的version
            List<ServiceInstance> greyInstances = chooeseNodes(version, instancesToChoose);

            // 没有找到对应节点,使用兜底节点master
            if (CollectionUtils.isEmpty(greyInstances)) {
                List<ServiceInstance> masters = chooeseNodes("master", instancesToChoose);
                if (!CollectionUtils.isEmpty(masters)) {
                    instancesToChoose = masters;
                }
            } else {
                instancesToChoose = greyInstances;
            }

            // 使用nacos的负载均衡算法
            ServiceInstance instance = NacosBalancer.getHostByRandomWeight3(instancesToChoose);
            log.info("GreyBalancer,命中机器服务 version: {},host: {},node-version={}",version, instance.getHost(),instance.getMetadata().get(Headers.VERSION));
            return new DefaultResponse(instance);
        } catch (Exception e) {
            log.warn("GreyBalancer error", e);
            return new EmptyResponse();
        }
    }

    private static List<ServiceInstance> chooeseNodes(String version, List<ServiceInstance> instancesToChoose) {
        return instancesToChoose.stream()
                .filter(serviceInstance -> {
                    // prod or grey
                    String configVersion = serviceInstance.getMetadata().get(Headers.VERSION);
                    return StringUtils.equals(version, configVersion);
                }).collect(Collectors.toList());
    }

    /**
     * 同一集群下优先获取,使用nacos的随机+权重算法。
     *
     * @param serviceInstances
     * @return
     */
    private Response<ServiceInstance> getClusterInstanceResponse(
            List<ServiceInstance> serviceInstances) {
        if (serviceInstances.isEmpty()) {
            log.warn("ClusterInstanceResponse:No servers available for service: {}",this.serviceId);
            return new EmptyResponse();
        }

        try {
            String clusterName = this.nacosDiscoveryProperties.getClusterName();

            List<ServiceInstance> instancesToChoose = serviceInstances;
            if (StringUtils.isNotBlank(clusterName)) {
                List<ServiceInstance> sameClusterInstances = serviceInstances.stream()
                        .filter(serviceInstance -> {
                            String cluster = serviceInstance.getMetadata().get("nacos.cluster");
                            return StringUtils.equals(cluster, clusterName);
                        }).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(sameClusterInstances)) {
                    instancesToChoose = sameClusterInstances;
                }
            } else {
                log.warn(
                        "A cross-cluster call occurs,name = {}, clusterName = {}, instance = {}",
                        serviceId, clusterName, serviceInstances);
            }

            ServiceInstance instance = NacosBalancer.getHostByRandomWeight3(instancesToChoose);
            log.info("LoadBalancer,同一集群下优先获取:命中机器服务 host {}", instance.getHost());
            return new DefaultResponse(instance);
        } catch (Exception e) {
            log.warn("LoadBalancer error", e);
            return new EmptyResponse();
        }
    }
}