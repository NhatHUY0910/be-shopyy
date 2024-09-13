package com.demo_shopyy_1.service.impl;

import com.demo_shopyy_1.config.VNPayConfig;
import com.demo_shopyy_1.model.Order;
import com.demo_shopyy_1.model.dto.OrderResponseDto;
import com.demo_shopyy_1.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {
    private static final Logger logger = LoggerFactory.getLogger(VNPayService.class);

    @Autowired
    private VNPayConfig vnPayConfig;

    @Autowired
    private OrderRepository orderRepository;

    public String createPaymentUrl(HttpServletRequest request, Order order) throws UnsupportedEncodingException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long amount = order.getTotalAmount().longValue() * 100;
        String bankCode = "";

//        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String vnp_TxnRef = UUID.randomUUID().toString();
        String vnp_IpAddress = getIpAddress(request);
        String vnp_TmnCode = vnPayConfig.getTmnCode();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + order.getId());
        vnp_Params.put("vnp_OrderType", orderType);

        String locale = request.getParameter("language");
        if (locale != null && !locale.isEmpty()) {
            vnp_Params.put("vnp_locale", locale);
        } else {
            vnp_Params.put("vnp_locale", "vn");
        }
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddress);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_expireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_expireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append("=");
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.getPaymentUrl() + "?" + queryUrl;
        logger.info("Generated VNPay URL: " + paymentUrl);
        return paymentUrl;
    }

    private String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException("key or data is null");
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 *result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("x-forwarded-for");
            if (ipAdress == null || ipAdress.length() == 0 || "unknown".equalsIgnoreCase(ipAdress)) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP: " + e.getMessage();
        }
        return ipAdress;
    }

    public boolean verifyPayment(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        String signValue = calculateSignValue(params);

        return signValue.equals(vnp_SecureHash);
    }

    private String calculateSignValue(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        fieldNames.remove("vnp_SecureHash");
        fieldNames.remove("vnp_SecureHashType");
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName).append("=").append(fieldValue);
                if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                    hashData.append("&");
                }
            }
        }

        return hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
    }

    public boolean processPaymentReturn(Map<String, String> vnpayParams) {
        logger.info("Processing VNPay payment return: {}", vnpayParams);
        try {
            if (verifyPayment(vnpayParams)) {
                String vnp_ResponseCode = vnpayParams.get("vnp_ResponseCode");
                String orderCode = vnpayParams.get("vnp_TxnRef");

                if ("00".equals(vnp_ResponseCode)) {
                    // Payment successful
                    updateOrderStatus(orderCode, Order.PaymentStatus.PAID);
                    logger.info("Payment successful for order: {}", orderCode);
                    return true;
                } else {
                    // Payment failed
                    updateOrderStatus(orderCode, Order.PaymentStatus.FAILED);
                    logger.warn("Payment failed for order: {}. Response code: {}", orderCode, vnp_ResponseCode);
                    return false;
                }
            }
            logger.error("Invalid payment signature for order: {}", vnpayParams.get("vnp_TxnRef"));
            return false;
        } catch (Exception e) {
            logger.error("Error processing payment return", e);
            return false;
        }
    }

    private void updateOrderStatus(String orderCode, Order.PaymentStatus status) {
        Order order = orderRepository.findByOrderCode(orderCode);
        if (order != null) {
            order.setPaymentStatus(status);
            orderRepository.save(order);
        }
    }
}