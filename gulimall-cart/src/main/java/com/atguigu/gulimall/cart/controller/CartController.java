package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItemVo;
import com.atguigu.gulimall.cart.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * @ClassName CartController
 * @Description: TODO
 * @Author fengjc
 * @Date 2020/12/30
 * @Version V1.0
 **/
@Controller
public class CartController {
    @Autowired
    private CartService cartService;

    /**
     * 获取购物车信息
     * 若用户未登录，则直接使用user-key获取购物车数据
     * 否则使用userId获取购物车数据，并将user-key对应临时购物车数据与用户购物车数据合并，并删除临时购物车
     * @param model
     * @return
     */
    @RequestMapping("/cart.html")
    public String getCartList(Model model) {
        CartVo cartVo=cartService.getCart();
        model.addAttribute("cart", cartVo);
        return "cartList";
    }
    @RequestMapping("/success.html")
    public String success() {
        return "success";
    }

    /**
     *添加商品到购物车
     * RedirectAttributes.addFlashAttribute():将数据放在session中，可以在页面中取出，但是只能取一次
     * RedirectAttributes.addAttribute():将数据放在url后面
     * @param skuId
     * @param num
     * @param attributes
     * @return
     */
    public String  addCartItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes attributes){
        cartService.addCartItem(skuId, num);
        attributes.addAttribute("skuId", skuId);
        //为了防止成功页刷新可以重复提交添加商品，我们不直接转到成功页,重定向到添加成功页面
        return "redirect:http://cart.gulimall.com/addCartItemSuccess";
    }
    @RequestMapping("/addCartItemSuccess")
    public String addCartItemSuccess(@RequestParam("skuId") Long skuId,Model model) {
        CartItemVo cartItemVo = cartService.getCartItem(skuId);
        model.addAttribute("cartItem", cartItemVo);
        return "success";
    }

    /**
     * 选中购物车项
     * @param isChecked
     * @param skuId
     * @return
     */
    @RequestMapping("/checkCart")
    public String checkCart(@RequestParam("isChecked") Integer isChecked,@RequestParam("skuId")Long skuId) {
        cartService.checkCart(skuId, isChecked);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 修改购物车商品数量
     * @param skuId
     * @param num
     * @return
     */
    @RequestMapping("/countItem")
    public String changeItemCount(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId, num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 删除购物车商品
     * @param skuId
     * @return
     */
    @RequestMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }
    /**
     * 删除购物车商品
     * @param skuId
     * @return
     */
    @RequestMapping("/deleteItem/all")
    public String deleteItemAll(@RequestParam("skuId") Long skuId) {
        cartService.deleteItemAll();
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 给订单服务提供查询选中的购物车商品接口
     * @return
     */
    @ResponseBody
    @RequestMapping("/getCheckedItems")
    public List<CartItemVo> getCheckedItems() {
        return cartService.getCheckedItems();
    }
}
