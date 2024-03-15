package cn.kwafoo.distributed.canary.grey;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancerClientConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * LoadBalancer负载均衡算法配置
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@Import(NacosLoadBalancerClientConfiguration.class)// 这里引入 nacos 默认客户端配置
public class ServiceLoadBalancerClientConfiguration {

    /**
     * local环境使用：本地优先策略,方便本地调试时分发到本地服务
     * 其他环境使用：nacos默认负载均衡策略
     *
     * @param environment               环境变量
     * @param loadBalancerClientFactory 工厂
     * @param nacosDiscoveryProperties  属性
     * @return ReactorLoadBalancer
     */
    @Bean
    public ReactorLoadBalancer<ServiceInstance> getLoadBalancer(Environment environment,
                                                                ObjectProvider<LoadBalancerClientFactory> loadBalancerClientFactory,
                                                                ObjectProvider<NacosDiscoveryProperties> nacosDiscoveryProperties) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        log.info("LoadBalancer,负载均衡算法使用:灰度负载均衡算法 for {}", name);
        return new GreyLoadBalancer(
                loadBalancerClientFactory.getIfAvailable().getLazyProvider(name,
                        ServiceInstanceListSupplier.class),
                name, nacosDiscoveryProperties.getIfAvailable());
    }

}