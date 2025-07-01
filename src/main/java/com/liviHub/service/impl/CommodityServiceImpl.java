package com.liviHub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liviHub.mapper.CommodityMapper;
import com.liviHub.model.entity.Commodity;
import com.liviHub.service.ICommodityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * todo 引入redis做缓存
 */
@Service
public class CommodityServiceImpl extends ServiceImpl<CommodityMapper, Commodity> implements ICommodityService {

    @Override
    public List<Commodity> listCommoditiesByShopId(Long shopId) {
        return list(new LambdaQueryWrapper<Commodity>()
                .eq(Commodity::getShopId, shopId)
                .orderByDesc(Commodity::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCommodity(Commodity commodity) {
        // 设置默认值
        if (commodity.getStatus() == null) {
            commodity.setStatus(1); // 默认上架状态
        }
        save(commodity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCommodity(Commodity commodity) {
        updateById(commodity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommodity(Long id) {
        removeById(id);
    }
}