package com.codegym.backend.service;

import com.codegym.backend.entity.Item;
import com.codegym.backend.repository.ItemRepository;
import com.codegym.backend.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @Override
    public List<Item> getLatestItems() {
        // Lấy trang đầu tiên (0), kích thước 4 phần tử
        return itemRepository.findByOrderByItemIdDesc(PageRequest.of(0, 4));
    }

    @Override
    public List<Item> getBestSellerItems() {
        // Lấy trang đầu tiên (0), kích thước 4 phần tử
        return itemRepository.findByOrderByTotalOrderCountDesc(PageRequest.of(0, 4));
    }
}