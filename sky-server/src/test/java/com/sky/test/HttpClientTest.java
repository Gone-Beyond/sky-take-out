package com.sky.test;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HttpClientTest {

    @Test
    public void testGET() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet("http://localhost:8080/user/shop/status");

        CloseableHttpResponse response = httpClient.execute(httpGet);

        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("服务端返回的状态码: " + statusCode);

        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity);

        System.out.println("服务端返回的数据为: " + body);

        response.close();

        httpClient.close();


    }











}
