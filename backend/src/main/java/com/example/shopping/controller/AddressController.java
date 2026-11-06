package com.example.shopping.controller;

import com.example.shopping.dto.ResponseDTO;
import com.example.shopping.entity.Address;
import com.example.shopping.entity.User;
import com.example.shopping.repository.AddressRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
public class AddressController {

    private final AddressRepository addressRepository;

    public AddressController(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<Address>>> getAddresses(@AuthenticationPrincipal User user) {
        List<Address> addresses = addressRepository.findByUserId(user.getId());
        return ResponseEntity.ok(ResponseDTO.success(addresses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<Address>> getAddressById(@AuthenticationPrincipal User user,
                                                              @PathVariable Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        if (!address.getUserId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(ResponseDTO.error("无权访问该地址"));
        }
        return ResponseEntity.ok(ResponseDTO.success(address));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<Address>> createAddress(@AuthenticationPrincipal User user,
                                                            @RequestBody Address address) {
        address.setUserId(user.getId());
        Address saved = addressRepository.save(address);
        return ResponseEntity.ok(ResponseDTO.success("地址添加成功", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<Address>> updateAddress(@AuthenticationPrincipal User user,
                                                            @PathVariable Long id,
                                                            @RequestBody Address address) {
        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        if (!existing.getUserId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(ResponseDTO.error("无权修改该地址"));
        }
        existing.setName(address.getName());
        existing.setPhone(address.getPhone());
        existing.setProvince(address.getProvince());
        existing.setCity(address.getCity());
        existing.setDistrict(address.getDistrict());
        existing.setAddress(address.getAddress());
        existing.setIsDefault(address.getIsDefault());
        Address updated = addressRepository.save(existing);
        return ResponseEntity.ok(ResponseDTO.success("地址更新成功", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteAddress(@AuthenticationPrincipal User user,
                                                          @PathVariable Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("地址不存在"));
        if (!address.getUserId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(ResponseDTO.error("无权删除该地址"));
        }
        addressRepository.deleteById(id);
        return ResponseEntity.ok(ResponseDTO.success("地址删除成功"));
    }
}
