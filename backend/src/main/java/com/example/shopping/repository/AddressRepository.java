
package com.example.shopping.repository;

import com.example.shopping.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserId(Long userId);

    Address findByUserIdAndIsDefaultTrue(Long userId);

    @Modifying
    @Query("DELETE FROM Address a WHERE a.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
