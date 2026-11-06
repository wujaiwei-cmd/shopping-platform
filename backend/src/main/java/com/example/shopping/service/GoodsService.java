
package com.example.shopping.service;

import com.example.shopping.dto.GoodsDTO;
import com.example.shopping.entity.Goods;
import com.example.shopping.repository.GoodsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    private final GoodsRepository goodsRepository;

    public GoodsService(GoodsRepository goodsRepository) {
        this.goodsRepository = goodsRepository;
    }

    public List<Goods> getAllGoods() {
        return goodsRepository.findByStatus("active");
    }

    public Goods getGoodsById(Long id) {
        return goodsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    public List<Goods> getGoodsByCategory(String category) {
        return goodsRepository.findByStatusAndCategory("active", category);
    }

    public List<Goods> searchGoods(String keyword) {
        return goodsRepository.searchByKeyword(keyword);
    }

    public Goods createGoods(GoodsDTO goodsDTO) {
        Goods goods = Goods.builder()
                .name(goodsDTO.getName())
                .price(goodsDTO.getPrice())
                .stock(goodsDTO.getStock() != null ? goodsDTO.getStock() : 0)
                .category(goodsDTO.getCategory())
                .description(goodsDTO.getDescription())
                .image(goodsDTO.getImage())
                .status(goodsDTO.getStatus() != null ? goodsDTO.getStatus() : "active")
                .build();

        return goodsRepository.save(goods);
    }

    public Goods updateGoods(Long id, GoodsDTO goodsDTO) {
        Goods goods = getGoodsById(id);

        if (goodsDTO.getName() != null) {
            goods.setName(goodsDTO.getName());
        }

        if (goodsDTO.getPrice() != null) {
            goods.setPrice(goodsDTO.getPrice());
        }

        if (goodsDTO.getStock() != null) {
            goods.setStock(goodsDTO.getStock());
        }

        if (goodsDTO.getCategory() != null) {
            goods.setCategory(goodsDTO.getCategory());
        }

        if (goodsDTO.getDescription() != null) {
            goods.setDescription(goodsDTO.getDescription());
        }

        if (goodsDTO.getImage() != null) {
            goods.setImage(goodsDTO.getImage());
        }

        if (goodsDTO.getStatus() != null) {
            goods.setStatus(goodsDTO.getStatus());
        }

        return goodsRepository.save(goods);
    }

    public void deleteGoods(Long id) {
        Goods goods = getGoodsById(id);
        goods.setStatus("inactive");
        goodsRepository.save(goods);
    }

    public Goods toggleGoodsStatus(Long id) {
        Goods goods = getGoodsById(id);
        goods.setStatus(goods.getStatus().equals("active") ? "inactive" : "active");
        return goodsRepository.save(goods);
    }
}
