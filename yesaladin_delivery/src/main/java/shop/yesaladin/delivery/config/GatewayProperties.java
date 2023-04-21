package shop.yesaladin.delivery.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 게이트웨이 관련 url를 가지는 설정파일
 *
 * @author 송학현
 * @since 1.0
 */
@Getter
@Configuration
public class GatewayProperties {

    @Value("${yesaladin.gateway.base}")
    private String gatewayUrl;

    @Value("${yesaladin.gateway.shop}")
    private String shopUrl;
}
