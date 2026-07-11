package com.codegym.backend.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.codegym.backend.dto.ItemResponse;
import com.codegym.backend.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAllItemsAsDTO();
    }

    @Override
    public List<ItemResponse> getLatestItems() {
        return itemRepository.findLatestItems(PageRequest.of(0, 4));
    }

    @Override
    public List<ItemResponse> getBestSellerItems() {
        return itemRepository.findBestSellerItems(PageRequest.of(0, 4));
    }
}