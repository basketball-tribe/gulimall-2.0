package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.common.excetion.NoStockException;
import com.atguigu.gulimall.common.to.mq.OrderTo;
import com.atguigu.gulimall.common.to.mq.SkuHasStockVo;
import com.atguigu.gulimall.common.to.mq.StockDetailTo;
import com.atguigu.gulimall.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.common.utils.R;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.enume.OrderStatusEnum;
import com.atguigu.gulimall.ware.enume.WareTaskStatusEnum;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gulimall.common.utils.PageUtils;
import com.atguigu.gulimall.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    private WareOrderTaskService wareOrderTaskService;
    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private OrderFeignService orderFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据商品id查询库存信息
     *
     * @param ids
     * @return
     */
    @Override
    public List<SkuHasStockVo> getSkuHasStocks(List<Long> ids) {
        List<SkuHasStockVo> collect = ids.stream().map(id -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            skuHasStockVo.setSkuId(id);
            Integer count = baseMapper.getTotalStock(id);
            skuHasStockVo.setHasStock(count == null ? false : count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 下单时锁库存 -> 锁了库存就意味着可能会出现问题就需要解库存，就需要延时队列来解决
     * @param wareSkuLockVo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo wareSkuLockVo) {
        //因为可能出现订单回滚后，库存锁定不回滚的情况，但订单已经回滚，得不到库存锁定信息，因此要有库存工作单
        //1.保存库存工作单
        WareOrderTaskEntity taskEntity =new WareOrderTaskEntity();
        taskEntity.setOrderSn(wareSkuLockVo.getOrderSn());
        taskEntity.setCreateTime(new Date());
        wareOrderTaskService.save(taskEntity);
        //2.查询出需要锁住的库存项
        List<OrderItemVo> itemVos = wareSkuLockVo.getLocks();//订单项
        List<SkuLockVo> lockVos = itemVos.stream().map((item) -> {
            SkuLockVo skuLockVo = new SkuLockVo();
            skuLockVo.setSkuId(item.getSkuId());
            skuLockVo.setNum(item.getCount());
            //找出所有库存大于商品数的仓库
            //只要有仓库的库存数大于购买的商品数就将这满足条件的仓库筛选出
            List<Long> wareIds = baseMapper.listWareIdsHasStock(item.getSkuId(), item.getCount());
            skuLockVo.setWareIds(wareIds);
            return skuLockVo;
        }).collect(Collectors.toList());
        //循环查询出每一个商品的库存信息
        for(SkuLockVo lockVo :lockVos){
            boolean lock =true;//默认锁库存成功
            Long skuId = lockVo.getSkuId();//库存商品id
            List<Long> wareIds = lockVo.getWareIds();//查询出仓库
            //如果没有满足条件的仓库，抛出异常
            if(wareIds == null || wareIds.size() ==0){
                throw  new NoStockException(skuId);
            }else {
                //循环查询出的仓库列表，将每个符合条件的仓库进行锁定
                for(Long wareId :wareIds){
                    //开始锁库存
                    Long count=baseMapper.lockWareSku(skuId, lockVo.getNum(), wareId);
                    if(count <= 0){
                        //说明没有更改库存信息,锁库存失败
                        lock =false;
                    }else {
                        //锁库存成功，保存工作单详情
                        WareOrderTaskDetailEntity detailEntity =WareOrderTaskDetailEntity.builder()
                                .skuId(skuId)
                                .skuName("")
                                .skuNum(lockVo.getNum())
                                .taskId(taskEntity.getId())
                                .wareId(wareId)
                                .lockStatus(1).build();
                        //保存工作单
                        wareOrderTaskDetailService.save(detailEntity);
                        //发送库存锁定消息到延迟队列
                        StockLockedTo lockedTo = new StockLockedTo();
                        lockedTo.setId(taskEntity.getId());
                        StockDetailTo detailTo = new StockDetailTo();
                        BeanUtils.copyProperties(detailEntity,detailTo);
                        lockedTo.setDetailTo(detailTo);
                        rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",lockedTo);
                        lock = true;
                        break;
                    }
                }
            }
            if (!lock) {
                throw new NoStockException(skuId);
            }
        }

        return true;
    }

    /**
     * 从锁库存成功之后进行的消息队列解锁库存
     * 先判断工作单中是否有这个订单详情，若不为空则说明库存锁定成功，可以进行解锁
     * 1没有订单 必须解锁库存
     *  2.有订单不一定要解锁库存，先查询订单状态：已取消则解锁库存，已支付不解锁库存
     * @param stockLockedTo
     */
    @Override
    public void unlock(StockLockedTo stockLockedTo) {
        //获取工作单信息
        StockDetailTo detailTo = stockLockedTo.getDetailTo();
        WareOrderTaskDetailEntity taskDetailEntity = wareOrderTaskDetailService.getById(detailTo.getId());
        //1.如果工作单详情不为空，说明该库存锁定成功
        if(taskDetailEntity != null){
            //根据库存量变化表查询出库存的订单信息
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(stockLockedTo.getId());
            //远程调用订单系统查询订单消息
            R r = orderFeignService.infoByOrderSn(taskEntity.getOrderSn());
            if(r.getCode() ==0){
                OrderTo order = r.getData("order", new TypeReference<OrderTo>() {
                });
                //解锁条件：没有订单详情 或者订单已经取消
                if(order == null || OrderStatusEnum.CANCLED.getCode().equals(order.getStatus())){
                    //为保证幂等性，保证工作单中必须是已锁定的项可以解锁
                    if(WareTaskStatusEnum.Locked.getCode().equals(taskDetailEntity.getLockStatus())){
                        //解锁需要几个参数:订单号 订单数量 仓库id
                        if(WareTaskStatusEnum.Locked.getCode().equals(taskDetailEntity.getLockStatus())){
                            unlockStock(detailTo.getSkuId(), detailTo.getSkuNum(), detailTo.getWareId(), taskDetailEntity.getId());

                        }

                    }
                }
            }
        }

    }

    /**
     * 订单结束之后的解库存服务
     * @param orderTo
     */
    @Override
    public void unlock(OrderTo orderTo) {
        //为防止重复解锁，需要重新查询工作单
        String orderSn = orderTo.getOrderSn();
        WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getBaseMapper().
                selectOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderSn));
        List<WareOrderTaskDetailEntity> lockDetails = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().
                eq("task_id", orderTaskEntity.getId()).eq("lock_status", WareTaskStatusEnum.Locked.getCode()));
           for(WareOrderTaskDetailEntity lockDetail:lockDetails){
               unlockStock(lockDetail.getSkuId(),lockDetail.getSkuNum(),lockDetail.getWareId(),lockDetail.getId());
           }
    }

    /**
     * 数据库中解锁库存
     * 为什么要解锁，是因为订单到时间后没有正常结束，可能是订单过期了，订单未支付，锁库存成功之后订单
     * @param skuId
     * @param skuNum
     * @param wareId
     * @param detailId
     */
    private void unlockStock(Long skuId, Integer skuNum, Long wareId, Long detailId) {
        //数据库中解锁库存
        baseMapper.unlockStock(skuId, skuNum, wareId);
        //更新库存工作单详情的状态
        WareOrderTaskDetailEntity detail = WareOrderTaskDetailEntity.builder()
                .id(detailId)
                .lockStatus(2).build();
        wareOrderTaskDetailService.updateById(detail);
    }

    @Data
    class SkuLockVo{
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }
}
