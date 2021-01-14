package com.atguigu.gulimall.seckill.controller;

import com.atguigu.gulimall.common.utils.R;
import com.atguigu.gulimall.seckill.service.SecKillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @ClassName SecKillController
 * @Description: TODO
 * @Author fengjc
 * @Date 2021/1/12
 * @Version V1.0
 **/
@Controller
public class SecKillController {
    @Autowired
    private SecKillService secKillService;
    /**
     * 当前时间可以参与秒杀的场次商品信息
     * @return
     */
    @GetMapping(value = "/getCurrentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus() {
        //获取到当前可以参加秒杀商品的信息
        List<SeckillSkuRedisTo> vos = secKillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    /**
     * 当前时间可以参与秒杀的商品信息
     * @param skuId
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/getSeckillSkuInfo/{skuId}")
    public R getSeckillSkuInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo to = secKillService.getSeckillSkuInfo(skuId);
        return R.ok().setData(to);
    }

    /**
     * 秒杀
      * @param killId  秒杀的活动场次id+商品id
     * @param key 秒杀的随机码
     * @param num  秒杀的数量
     * @param model
     * @return
     */
    @GetMapping("/kill")
    public String kill(@RequestParam("killId") String killId,
                       @RequestParam("key")String key,
                       @RequestParam("num")Integer num,
                       Model model) {
        String orderSn= null;
        try {
            orderSn = secKillService.kill(killId, key, num);
            model.addAttribute("orderSn", orderSn);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "success";
    }
}
