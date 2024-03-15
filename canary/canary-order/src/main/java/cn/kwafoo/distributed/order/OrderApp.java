package cn.kwafoo.distributed.order;

import cn.kwafoo.distributed.canary.grey.EnableGrey;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @description:
 * @author: zk
 * @date: 2024-03-13
 */
@EnableGrey
@EnableFeignClients({"cn.kwafoo.distributed.stock.**"})
@EnableDiscoveryClient
@SpringBootApplication
public class OrderApp {
    public static void main(String[] args) {
        SpringApplication.run(OrderApp.class);
    }
}
