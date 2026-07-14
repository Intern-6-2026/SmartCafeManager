package com.codegym.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.codegym.backend.dto.ItemProjection;
import com.codegym.backend.dto.ItemResponse;
import com.codegym.backend.repository.ItemRepository;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAllItemsAsDTO(PageRequest.of(0, 9)).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    @Override
    public List<ItemResponse> getLatestItems() {
        return itemRepository.findLatestItems(PageRequest.of(0, 4)).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    @Override
    public List<ItemResponse> getBestSellerItems() {
        return itemRepository.findBestSellerItems(PageRequest.of(0, 4)).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    private ItemResponse convertToResponse(ItemProjection projection) {
        return ItemResponse.builder()
                .itemId(projection.getItemId())
                .itemCode(projection.getItemCode())
                .categoryId(projection.getCategoryId())
                .categoryName(projection.getCategoryName())
                .itemName(projection.getItemName())
                .price(projection.getPrice())
                .description(projection.getDescription())
                .imageUrl(projection.getImageUrl())
                .isAvailable(projection.getIsAvailable())
                .totalOrderCount(projection.getTotalOrderCount() != null ? projection.getTotalOrderCount().longValue() : 0L)
                .build();
    }
}