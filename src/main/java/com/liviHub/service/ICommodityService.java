package com.liviHub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liviHub.model.entity.Commodity;

import java.util.List;

public interface ICommodityService extends IService<Commodity> {
    /**
     * 根据商铺ID查询商品列表
     * @param shopId 商铺ID
     * @return 商品列表
     */
    List<Commodity> listCommoditiesByShopId(Long shopId);

    /**
     * 添加商品
     * @param commodity 商品信息
     */
    void addCommodity(Commodity commodity);

    /**
     * 更新商品
     * @param commodity 商品信息
     */
    void updateCommodity(Commodity commodity);

    /**
     * 删除商品
     * @param id 商品ID
     */
    void deleteCommodity(Long id);
}