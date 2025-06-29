package com.liviHub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_commodity")
public class Commodity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private Long categoryId;
    private String name;
    private String description;
    private Long originalPrice;
    private Long currentPrice;
    private Integer stock;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}