package cn.kwafoo.distributed.order.controller;

import cn.kwafoo.common.constant.Headers;
import cn.kwafoo.common.resp.Result;
import cn.kwafoo.distributed.stock.api.StockApi;
import com.alibaba.nacos.api.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * @description:
 * @author: zk
 * @date: 2024-03-13
 */
@Slf4j
@RestController
public class OrderController {
    @Autowired
    private StockApi stockApi;

    @Value("${server.port}")
    private String port;

    @PostMapping("/order")
    public Result order(String param) {
        String orderNo = UUID.randomUUID().toString().replace("-", "");
        log.info("下单业务------>ip:{}:{},param={},orderNo={},version:{}", NetUtils.localIP(),port,param,orderNo,getHeader(Headers.VERSION));
        Result stockApiResult = stockApi.decrease(orderNo);
        return Result.ok();
    }

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static String getHeader(String header) {
        return getRequest().getHeader(header);
    }

}
