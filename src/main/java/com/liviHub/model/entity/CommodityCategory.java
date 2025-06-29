package com.liviHub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("tb_commodity_category")
public class CommodityCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
}