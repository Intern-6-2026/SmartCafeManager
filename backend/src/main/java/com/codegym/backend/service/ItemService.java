package com.codegym.backend.service;

import java.util.List;

import com.codegym.backend.dto.ItemResponse;
import com.codegym.backend.entity.Item;

public interface ItemService {
    List<ItemResponse> getAllItems();
    List<ItemResponse> getLatestItems();
    List<ItemResponse> getBestSellerItems();
}