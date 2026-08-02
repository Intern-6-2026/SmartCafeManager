package com.codegym.backend.entity;

import java.math.BigDecimal;
import java.util.Date;
import com.codegym.backend.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

@Entity
@jakarta.persistence.Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "salary", nullable = false)
    private BigDecimal salary;

    @Column(name = "image_url")
    private String imageUrl;
}