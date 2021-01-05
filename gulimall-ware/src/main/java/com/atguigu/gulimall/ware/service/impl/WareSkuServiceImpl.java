package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.gulimall.common.excetion.NoStockException;
import com.atguigu.gulimall.common.to.mq.SkuHasStockVo;
import com.atguigu.gulimall.common.to.mq.StockDetailTo;
import com.atguigu.gulimall.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
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
     * 下单时锁库存
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
        List<OrderItemVo> itemVos = wareSkuLockVo.getLocks();
        List<SkuLockVo> lockVos = itemVos.stream().map((item) -> {
            SkuLockVo skuLockVo = new SkuLockVo();
            skuLockVo.setSkuId(item.getSkuId());
            skuLockVo.setNum(item.getCount());
            //找出所有库存大于商品数的仓库
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
                        wareOrderTaskDetailService.save(detailEntity);//保存工作单
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
    @Data
    class SkuLockVo{
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }
}
