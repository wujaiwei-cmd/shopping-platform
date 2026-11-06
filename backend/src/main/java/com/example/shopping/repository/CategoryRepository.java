
package com.example.shopping.repository;

import com.example.shopping.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByEnabledTrueOrderBySortOrder();

    Optional<Category> findByName(String name);

    boolean existsByName(String name);
}
