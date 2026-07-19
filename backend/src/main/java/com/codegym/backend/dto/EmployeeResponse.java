package com.codegym.backend.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeResponse {
    private Long employeeId;
    private String accountId;
    private String userName;
    private String fullName;
    private String email;
    private Date dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String address;
    private BigDecimal salary;
    private String imageUrl;
    private String roleName;
    private String accountStatus;
}
