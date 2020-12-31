package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.exception.NoStockException;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @ClassName OrderWebController
 * @Description: TODO
 * @Author fengjc
 * @Date 2020/12/30
 * @Version V1.0
 **/
@Controller
public class OrderWebController {
    @Autowired
    private OrderService orderService;

    /**
     * 订单获取数据 跳转到订单结算页
     * * 查询购物项、库存和收货地址都要调用远程服务，串行会浪费大量时间，因此我们使用CompletableFuture进行异步编排
     * * 可能由于延迟，订单提交按钮可能被点击多次，为了防止重复提交的问题，我们在返回订单确认页时，在redis中生成一个随机的令牌，
     * * 过期时间为30min，提交的订单会携带这个令牌，我们将会在订单提交的处理页面核验此令牌
     *
     * @param model
     * @return
     */
    @RequestMapping("/toTrade")
    public String toTrade(Model model) {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("confirmOrder", confirmVo);
        return "confirm";
    }

    /**
     * 订单提交按钮
     * 提交订单成功，则携带返回数据转发至支付页面
     * 提交订单失败，则携带错误信息重定向至确认页
     *
     * @return
     */
    @RequestMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo submitVo, Model model, RedirectAttributes attributes) {
        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(submitVo);
            Integer code = responseVo.getCode();
            if (code == 0) {
                model.addAttribute("order", responseVo.getOrder());
                return "pay";
            }else {
                String msg = "下单失败";
                switch (code){
                    case 1:
                        msg += "防重令牌校验失败";
                        break;
                    case 2:
                        msg += "商品价格发生变化";
                        break;
                }
                attributes.addFlashAttribute("msg", msg);
                //下单失败后重定向至确认页
                return "redirect:http://order.gulimall.com/toTrade";
            }
        } catch (Exception e) {
            if (e instanceof NoStockException){
                String msg = "下单失败，商品无库存";
                attributes.addFlashAttribute("msg", msg);
            }
            //下单失败后重定向至确认页
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
