package cn.kwafoo.distributed.canary.gateway;

import cn.kwafoo.distributed.canary.grey.EnableGrey;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @description:
 * @author: zk
 * @date: 2024-03-13
 */
@EnableGrey
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApp {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApp.class);
    }
}
