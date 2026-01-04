package com.nvminh162.identity.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/*
Nếu muốn bất kỳ request nào của feign client đều gắn interceptor thì gắn @Component
Trong thực tế có nhiều client khác nhau (connect ra bên ngoài gọi API vào) => TH sử dụng nếu app không request connect ra bên ngoài (internal)
-> Gỡ @Component trong TH này khi cần thì gọi
*/
// @Component
@Slf4j
public class AuthenticationRequestInterceptor implements RequestInterceptor {
    /*
    * Modify request trước khi submit*/
    @Override
    public void apply(RequestTemplate template) {
        // lấy token
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        log.info(">>> Authorization header: {}", authHeader);

        if (StringUtils.hasText(authHeader)) {
            template.header("Authorization", authHeader);
            log.info(">>> Authorization header added to request");
        }
    }
}
