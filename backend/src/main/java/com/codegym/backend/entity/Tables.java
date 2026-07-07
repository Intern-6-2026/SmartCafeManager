package com.codegym.backend.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.codegym.backend.enums.PhysicalState;
import com.codegym.backend.enums.ServiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tables extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long table_id;

    @Column(name = "table_name", nullable = false, unique = true)
    private String table_name;

    @Enumerated(EnumType.STRING)
    @Column(name = "physical_state", nullable = false)
    private PhysicalState physical_state;

    @Column(name = "is_occupied", nullable = false)
    private Boolean is_occupied;

    @Column(name = "service_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceStatus service_status;
}
