package com.liviHub.controller;

import com.liviHub.model.dto.Result;
import com.liviHub.model.entity.Commodity;
import com.liviHub.service.ICommodityService;
import jakarta.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commodity")
public class CommodityController {

    @Resource
    private ICommodityService commodityService;


    @PostMapping("/add")
    public Result addCommodity(@RequestBody Commodity commodity) {
        commodityService.addCommodity(commodity);
        return Result.ok("商品添加成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteCommodity(@PathVariable Long id) {
        commodityService.deleteCommodity(id);
        return Result.ok("商品删除成功");
    }

    @PutMapping("/update")
    public Result updateCommodity(@RequestBody Commodity commodity) {
        commodityService.updateCommodity(commodity);
        return Result.ok("商品更新成功");
    }

    @GetMapping("/list/{shopId}")
    public Result listCommoditiesByShopId(@PathVariable Long shopId) {
        List<Commodity> commodities = commodityService.listCommoditiesByShopId(shopId);
        return Result.ok(commodities);
    }
}