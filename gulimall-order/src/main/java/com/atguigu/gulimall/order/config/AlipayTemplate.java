package com.atguigu.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName AlipayTemplate
 * @Description: TODO
 * @Author fengjc
 * @Date 2021/1/25
 * @Version V1.0
 **/
@ConfigurationProperties(prefix = "alipay")
@Data
@Configuration
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000117604393";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCmQtLfXkqIkhfmjP+9rBhnthGbs9JT6nU5MhjtfL9A/4uSt1g4Y0GOKq3rsNCr/VRxDdOaOIOeAoMl4u/tWrEI4UuXKC55IZjE1lWQYUOneT9D03UyP6NjoC4YOb+Peb5quAe15gQETlbiu3MWSkfJ3EzdmVwbRQQ1fPi6mzseSMNUejuFWH7iqjCXn/8Rr/abbUuZ+EN7QtqMypuf5L3nnDL90/u8eNF5dj3CgjgSGd5y+zQkXoHbf5ofeAnC/UVfGRJgJKfIAi51fMkirXOlSgtvwglLGcvEclpP5mAoq68HUTzkZXV7++qFeON6IEmd5FbNwwWcK+AnDx58g6b3AgMBAAECggEAegMqO7A35HfKQHCTMb+8/UdZLrnNjbXPJxVDw+07cdjXtekFPgRhVJKvpfrReJZlxL3P3yrANFJFqqUbTEW1C/5CegCL8msccL+WdWlbu+i36++f3ytinbcsfKzMjt9aRXked3gA1KQlESldMSt7+YDJjAqC1KRWjWZUJqXKwdKTTC+snPWxn4uJhyOb+VlxBjdDnb8v9ja7AXF5f1KkJjJTM7jznEK0IKJOd+aFAcuv+gQhIk10P8hbVZQgSFCoRkjLh4jFGHoThP3n0x569Tr5gMfkrcvCOGhjrIff/yoxs7vgGXKDVyWpcq+Z0Xrd0hdDON7iJ29mcgYqXZixUQKBgQDzWxr/CVm7+SCZYr9Oyl7LUWGjMfzdRoD8hjBmEsGBuAZiYk33TKsUwpgJakss+57yPN+HX86Oy4xY1N1nhONceJvgrKrDDvm+F9KqDac7I69hHpio1Y2rzeityG9173+1HHLsjiQ/tISxopK4d8x0YG1YeCxJjQgE/CPX9cDoHwKBgQCu5kYikHRRTnZSXPqazSKlVmdOgmZufzLB4htRve+yfyBoZGZYS9oQDTTwXbPwHZJe72pbDiQ7/9W6lnOTZ9UPwdCIwPJRg5+Ct/E4ETTLeGQIlGx5OlF5DMHZrtGANtmbTL2vmkg7YsjzW6r1t4BRvvO8p9YMlghlfSygchpGKQKBgEEzfLUuNSum4PQXv/gFQoapBEbsQrqqhC6kaV0/0zbFLni3q/oBQakrRf7nysJ/nCN86crkgXxCzxSR28w7j4scCW+V856VaZZj3Z6QT+kJc5jK0M4TgYSxg5DkLfDkPUwyFhyJLO8gl5jcXQEtn8ridwmIER2XurlTMOtQveGzAoGAYvjQK3MPP4tlDYqsDbmTp3hTkWvdRx4XvPuUdhdDcwDZd/mIqOKTjY3LrVF5J6G0WKCm0MwZ2RIXAjVtRbb2LKqCtUBqX5JaS4CHsjCabaG9CdXj5F4KCLQdZm+1AhcH4Qf2N07ZHTHzgTMYdV/X72pdRx660/h62+OPw2OOVrECgYEA6djeyu4eKkBvrrPx3mi2C5+pE9aEP1qArcdZJ7nNkaVN4IhnBkR1vFOnq01sKjp9Xk5eX2quiq/iPop8f14c4DHJg8MFw8BodZXQnxrFuIaUng/LMQwGRMduKcnt8JS9lbsCw0AplK39snjXlRpYRv0GGUYX+bAp536dIu3rDe0=";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhfb56x0TNn9QAK1MPGoUQPtc4MDmqOKTLqPpb/eGqxsfzXRd6jGxX3dgMImmm599YWdOUtQzeMzcmLZUIsrkDTUl9KtlhChW9tOtOXjjfKn3ApzX86vV5L9UBon/I5chPfHY0kuaDvDH/d5PKvxAhko10ZwcT2TSkjpvgfYh0Bc8rbhK+viup29TTKG5Zue1A1jDcoT1KuRBOke97DGk+/Tf7NhT5Eo/Kc8Y/ef2rwcwZeqmznXKgClIJG2twkws/ecVqjlIQOGh9oNgohhO17JRH6ZVY5pLJDjkkkRokn5vqgf9VtSpuDZdZYN7fjVma2P0n3CV3MSeObTAPgTa3QIDAQAB";

    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url="http://localhost:9000/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url="http://localhost:9000/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                +"\"timeout_express\":\"1m\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
