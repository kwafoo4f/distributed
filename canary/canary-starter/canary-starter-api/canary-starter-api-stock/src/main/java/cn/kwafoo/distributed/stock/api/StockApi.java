package cn.kwafoo.distributed.stock.api;

import cn.kwafoo.common.resp.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @description:
 * @author: zk
 * @date: 2024-03-13
 */
@FeignClient(value = "canary-stock")
public interface StockApi {

    /**
     * 扣减库存
     *
     * @return
     */
    @PostMapping("/stock/decrease")
    Result decrease(@RequestBody String orderNo);

}
