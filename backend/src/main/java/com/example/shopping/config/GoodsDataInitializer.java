package com.example.shopping.config;

import com.example.shopping.entity.Goods;
import com.example.shopping.repository.GoodsRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class GoodsDataInitializer implements CommandLineRunner {

    private final GoodsRepository goodsRepository;

    public GoodsDataInitializer(GoodsRepository goodsRepository) {
        this.goodsRepository = goodsRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (goodsRepository.count() > 0) {
            return;
        }

        goodsRepository.save(Goods.builder()
                .name("Smartphone")
                .price(BigDecimal.valueOf(2999.00))
                .stock(50)
                .category("Electronics")
                .description("High-performance smartphone with clear camera")
                .image("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400")
                .status("active")
                .build());

        goodsRepository.save(Goods.builder()
                .name("Gaming Phone")
                .price(BigDecimal.valueOf(3999.00))
                .stock(30)
                .category("Electronics")
                .description("Specially designed for gaming")
                .image("https://images.unsplash.com/photo-1605236453806-6ff36851218e?w=400")
                .status("active")
                .build());

        goodsRepository.save(Goods.builder()
                .name("Apple iPhone")
                .price(BigDecimal.valueOf(8999.00))
                .stock(20)
                .category("Electronics")
                .description("Latest Apple iPhone")
                .image("https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=400")
                .status("active")
                .build());

        goodsRepository.save(Goods.builder()
                .name("Calcium Tablets")
                .price(BigDecimal.valueOf(99.00))
                .stock(100)
                .category("Health")
                .description("Calcium supplement for bone health")
                .image("https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=400")
                .status("active")
                .build());

        goodsRepository.save(Goods.builder()
                .name("Vitamin C")
                .price(BigDecimal.valueOf(49.00))
                .stock(200)
                .category("Health")
                .description("Vitamin C supplement to boost immunity")
                .image("https://images.unsplash.com/photo-1505576399279-565b52d4ac71?w=400")
                .status("active")
                .build());

        goodsRepository.save(Goods.builder()
                .name("Energy Drink")
                .price(BigDecimal.valueOf(5.00))
                .stock(500)
                .category("Food")
                .description("Energy and hydration supplement")
                .image("https://images.unsplash.com/photo-1527960471264-932f39eb5846?w=400")
                .status("active")
                .build());

        System.out.println("Goods data initialized successfully");
    }
}
