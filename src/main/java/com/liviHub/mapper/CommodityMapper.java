package com.liviHub.mapper;
//商品Mapper接口
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.liviHub.model.entity.Commodity;


@Mapper
public interface CommodityMapper extends BaseMapper<Commodity> {

}