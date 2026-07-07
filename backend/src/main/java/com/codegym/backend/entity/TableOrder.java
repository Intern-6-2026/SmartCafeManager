package com.codegym.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.codegym.backend.enums.PaymentMethod;
import com.codegym.backend.enums.StatusTableOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "table_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long table_order_id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "table_id", referencedColumnName = "table_id")
    private Tables table_id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
    private Customer customer_id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id")
    private Employee employee_id;

    @Column(name = "open_at", nullable = false)
    private LocalDateTime open_at;

    @Column(name = "close_at", nullable = false)
    private LocalDateTime close_at;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal total_amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod payment_method;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paid_at;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusTableOrder status;

}
