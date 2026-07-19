package com.codegym.backend.dto;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeCreateRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String userName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Tên đầy đủ không được để trống")
    private String fullName;

    @NotBlank(message = "Ngày sinh không được để trống")
    private Date dateOfBirth;

    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotBlank(message = "Vai trò không được để trống")
    private Long roleId;

    private String gender;
    private String address;
    private BigDecimal salary;
    private String imageUrl;
}
