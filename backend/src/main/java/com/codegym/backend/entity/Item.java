package com.codegym.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@jakarta.persistence.Table(name = "item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_code", nullable = false, unique = true)
    private String itemCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private MenuCategory category;

    @Column(name = "item_name", nullable = false, unique = true)
    private String itemName;

    @Column(name = "price", nullable = false, precision = 15, scale = 0)
    private BigDecimal price;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @Column(name = "total_order_count", nullable = false)
    private Integer totalOrderCount;
}