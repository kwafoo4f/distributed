package cn.kwafoo.distributed.canary.grey;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启灰度发布
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ServiceLoadBalancerClientConfiguration.class)
@LoadBalancerClients(defaultConfiguration = ServiceLoadBalancerClientConfiguration.class)
public @interface EnableGrey {
}
