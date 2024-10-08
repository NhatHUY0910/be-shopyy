package com.demo_shopyy_1.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VNPayConfig {

//    @Value("${vnpay.tmnCode}")
    private String tmnCode;

//    @Value("${vnpay.hashSecret}")
    private String hashSecret;

//    @Value("${vnpay.paymentUrl}")
    private String paymentUrl;

//    @Value("${vnpay.returnUrl}")
    private String returnUrl;
}
