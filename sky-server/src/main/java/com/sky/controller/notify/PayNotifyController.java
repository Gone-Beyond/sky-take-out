package com.sky.controller.notify;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.properties.WeChatProperties;
import com.sky.service.OrderService;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付回调相关接口。
 */
@RestController
@RequestMapping("/notify")
@Slf4j
public class PayNotifyController {

    private final OrderService orderService;
    private final WeChatProperties weChatProperties;

    public PayNotifyController(OrderService orderService, WeChatProperties weChatProperties) {
        this.orderService = orderService;
        this.weChatProperties = weChatProperties;
    }

    /**
     * 支付成功回调。
     */
    @RequestMapping("/paySuccess")
    public void paySuccessNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String body = readData(request);
        log.info("微信支付成功回调，原始数据：{}", body);

        String plainText = decryptData(body);
        log.info("微信支付成功回调，解密数据：{}", plainText);

        JSONObject jsonObject = JSON.parseObject(plainText);
        String outTradeNo = jsonObject.getString("out_trade_no");
        String transactionId = jsonObject.getString("transaction_id");

        log.info("商户订单号：{}", outTradeNo);
        log.info("微信支付交易号：{}", transactionId);

        orderService.paySuccess(outTradeNo);
        responseToWeiXin(response);
    }

    /**
     * 读取微信回调请求体。
     */
    private String readData(HttpServletRequest request) throws Exception {
        BufferedReader reader = request.getReader();
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (result.length() > 0) {
                result.append("\n");
            }
            result.append(line);
        }
        return result.toString();
    }

    /**
     * 使用 apiV3Key 解密微信支付回调中的 resource 数据。
     */
    private String decryptData(String body) throws Exception {
        JSONObject resultObject = JSON.parseObject(body);
        JSONObject resource = resultObject.getJSONObject("resource");
        String ciphertext = resource.getString("ciphertext");
        String nonce = resource.getString("nonce");
        String associatedData = resource.getString("associated_data");

        AesUtil aesUtil = new AesUtil(weChatProperties.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        return aesUtil.decryptToString(
                associatedData.getBytes(StandardCharsets.UTF_8),
                nonce.getBytes(StandardCharsets.UTF_8),
                ciphertext
        );
    }

    /**
     * 按微信支付要求返回成功响应。
     */
    private void responseToWeiXin(HttpServletResponse response) throws Exception {
        response.setStatus(200);
        Map<String, String> result = new HashMap<>();
        result.put("code", "SUCCESS");
        result.put("message", "SUCCESS");
        response.setHeader("Content-type", ContentType.APPLICATION_JSON.toString());
        response.getOutputStream().write(JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
    }
}
