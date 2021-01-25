package com.atguigu.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @ClassName PayWebController
 * @Description: TODO
 * @Author fengjc
 * @Date 2021/1/25
 * @Version V1.0
 **/
@Controller
@RequestMapping("/payed")
public class PayWebController {
    @Autowired
    private AlipayTemplate alipayTemplate;
    @Autowired
    private OrderService orderService;
    @ResponseBody
    @GetMapping(value = "/aliPayOrder",produces = "text/html")
    public String aliPayOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        System.out.println("接收到订单信息orderSn："+orderSn);
        PayVo payVo = orderService.getOrderPay(orderSn);
//        PayVo payVo = new PayVo();
//        payVo.setBody("洗衣粉");
//        payVo.setOut_trade_no("20210011001111");
//        payVo.setSubject("购买商品");
//        payVo.setTotal_amount("1001.00");
        String pay = alipayTemplate.pay(payVo);
        return pay;
    }
}
