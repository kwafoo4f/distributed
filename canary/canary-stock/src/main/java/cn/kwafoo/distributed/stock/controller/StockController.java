package cn.kwafoo.distributed.stock.controller;

import cn.kwafoo.common.constant.Headers;
import cn.kwafoo.common.resp.Result;
import cn.kwafoo.distributed.stock.api.StockApi;
import com.alibaba.nacos.api.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @description:
 * @author: zk
 * @date: 2024-03-15
 */
@Slf4j
@RestController
public class StockController implements StockApi {

    @Value("${server.port}")
    private String port;
    @Override
    @PostMapping("/stock/decrease")
    public Result decrease(@RequestBody String orderNo) {
        log.info("库存业务------>ip:{}:{},orderNo={},version:{}", NetUtils.localIP(),port,orderNo,getHeader(Headers.VERSION));
        return Result.ok();
    }

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static String getHeader(String header) {
        return getRequest().getHeader(header);
    }

}
