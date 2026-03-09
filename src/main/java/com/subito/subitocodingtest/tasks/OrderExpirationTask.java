package com.subito.subitocodingtest.tasks;

import com.subito.subitocodingtest.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderExpirationTask {

    private final OrderService orderService;

    public OrderExpirationTask(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(cron = "0 * * * * *") // Runs every minute
    public void expireUnpaidOrders() {
        log.info("Running scheduled task to expire unpaid orders.");
        orderService.expireUnpaidOrders();
    }
}
