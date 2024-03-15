package cn.kwafoo.distributed.order.config;

import cn.kwafoo.common.constant.Headers;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Target;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * @description: feign拦截器, 请求中设置公共请求头
 * @author: zk
 * @date: 2023-06-08 11:14
 */
@Slf4j
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 将请求中的请求头设置到requestTemplate中
        Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .map(servletRequestAttributes -> (ServletRequestAttributes) servletRequestAttributes)
                .map(ServletRequestAttributes::getRequest)
                .ifPresent(request -> setHeader(requestTemplate, request));

        String serviceName = Optional.ofNullable(requestTemplate).map(RequestTemplate::feignTarget).map(Target::name).orElse("");
        log.info("[feign远程调用],serviceName={},path={}", serviceName, requestTemplate.path());
    }

    /**
     * 设置请求头
     *
     * @param requestTemplate
     * @param request
     */
    private void setHeader(RequestTemplate requestTemplate, HttpServletRequest request) {
        setHeaderIfPresent(Headers.VERSION, requestTemplate, request);
    }

    private void setHeaderIfPresent(String headerName, RequestTemplate requestTemplate, HttpServletRequest request) {
        Optional.ofNullable(request)
                .map(servletRequest -> servletRequest.getHeader(headerName))
                .ifPresent(headerValue -> {
                    requestTemplate.header(headerName, headerValue);
                });
    }

    private void setHeader(String headerName, RequestTemplate requestTemplate, HttpServletRequest request, String defaultValue) {
        String headerValue = Optional.ofNullable(request)
                .map(servletRequest -> servletRequest.getHeader(headerName))
                .orElse(defaultValue);
        requestTemplate.header(headerName, headerValue);
    }

}
