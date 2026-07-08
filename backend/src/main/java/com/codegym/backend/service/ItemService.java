package com.codegym.backend.service;

import java.util.List;

import com.codegym.backend.entity.Item;

public interface ItemService {
    List<Item> getAllItems(); // Hàm ra toàn bộ danh sách sản phẩm
    List<Item> getLatestItems();      // Hàm lấy món mới nhất
    List<Item> getBestSellerItems();  // Hàm lấy món bán chạy
}