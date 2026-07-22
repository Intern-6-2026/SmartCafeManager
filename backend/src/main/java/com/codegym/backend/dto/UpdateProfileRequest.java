package com.codegym.backend.dto;

import java.util.Date;
import com.codegym.backend.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.NoArgsConstructor;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProfileRequest {
    @NotBlank(message = "Full name cannot be empty")
    private String fullName;

    private Date dateOfBirth;

    private Gender gender;

    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Phone number is not in the correct Vietnamese format")
    private String phoneNumber;

    private String address;

    private String imageUrl;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email format is not valid")
    private String email;
}