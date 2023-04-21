package shop.yesaladin.delivery.scheduler;

import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import shop.yesaladin.common.dto.ResponseDto;
import shop.yesaladin.delivery.common.dto.ResultCodeDto;
import shop.yesaladin.delivery.config.GatewayProperties;
import shop.yesaladin.delivery.transport.domain.model.Transport;
import shop.yesaladin.delivery.transport.service.event.TransportCompleteEventListener;
import shop.yesaladin.delivery.transport.service.inter.TransportService;

/**
 * Shop API 서버의 주문 상태 변경 이력 테이블에 배송 완료 상태를 추가하기 위한 스케줄러 입니다.
 *
 * @author 송학현
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderStatusChangeScheduler {

    private static final String EXECUTE_CRON = "0 0/3 * * * ?";
    private final TransportService transportService;
    private final RestTemplate restTemplate;
    private final GatewayProperties gatewayProperties;
    private ThreadPoolTaskScheduler scheduler;

    private Runnable getRunnable() {
        return () -> {
            while (!TransportCompleteEventListener.orderIdQueue.isEmpty()) {
                Long orderId = TransportCompleteEventListener.orderIdQueue.poll();
                Transport transport = transportService.getLatestTransport(orderId);
                if (Objects.nonNull(transport)) {
                    log.info("latest transport orderId={}", transport.getOrderId());
                    log.info("status={}", transport.getTransportStatusCode());

                    // restTemplate call
                    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(
                                    gatewayProperties.getShopUrl()
                                            + "/v1/orders/{orderId}/delivery-complete")
                            .build()
                            .expand(orderId);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<String> entity = new HttpEntity<>(headers);

                    ResponseEntity<ResponseDto<ResultCodeDto>> responseEntity = restTemplate.exchange(
                            uriComponents.toUri(),
                            HttpMethod.POST,
                            entity,
                            new ParameterizedTypeReference<>() {
                            }
                    );
                    ResultCodeDto data = Objects.requireNonNull(responseEntity.getBody().getData());
                    log.info("API Call result={}", data.getResult());
                }
            }
        };
    }

    public void startScheduler() {
        this.scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.setWaitForTasksToCompleteOnShutdown(false);
        scheduler.schedule(getRunnable(), getTrigger());
    }

    public void stopScheduler() {
        this.scheduler.shutdown();
    }

    private Trigger getTrigger() {
        return new CronTrigger(EXECUTE_CRON);
    }

    @PostConstruct
    public void init() {
        startScheduler();
    }

    @PreDestroy
    public void destroy() {
        stopScheduler();
    }
}
