
package com.example.shopping.controller;

import com.example.shopping.dto.GoodsDTO;
import com.example.shopping.dto.ResponseDTO;
import com.example.shopping.entity.Goods;
import com.example.shopping.service.GoodsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goods")
@CrossOrigin(origins = "*")
public class GoodsController {

    private final GoodsService goodsService;

    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<Goods>>> getAllGoods(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        List<Goods> goods;

        if (keyword != null && !keyword.isEmpty()) {
            goods = goodsService.searchGoods(keyword);
        } else if (category != null && !category.isEmpty()) {
            goods = goodsService.getGoodsByCategory(category);
        } else {
            goods = goodsService.getAllGoods();
        }

        return ResponseEntity.ok(ResponseDTO.success(goods));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<Goods>> getGoodsById(@PathVariable Long id) {
        try {
            Goods goods = goodsService.getGoodsById(id);
            return ResponseEntity.ok(ResponseDTO.success(goods));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<Goods>> createGoods(@Valid @RequestBody GoodsDTO goodsDTO) {
        try {
            Goods goods = goodsService.createGoods(goodsDTO);
            return ResponseEntity.ok(ResponseDTO.success("商品创建成功", goods));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<Goods>> updateGoods(@PathVariable Long id, @Valid @RequestBody GoodsDTO goodsDTO) {
        try {
            Goods goods = goodsService.updateGoods(id, goodsDTO);
            return ResponseEntity.ok(ResponseDTO.success("商品更新成功", goods));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteGoods(@PathVariable Long id) {
        try {
            goodsService.deleteGoods(id);
            return ResponseEntity.ok(ResponseDTO.success("商品已下架"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
        }
    }
}
