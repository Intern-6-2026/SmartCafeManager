package com.codegym.backend.DTO;

import java.util.Date;
import com.codegym.backend.enums.Gender;
import lombok.*;

@Getter
@Setter
public class UpdateProfileRequest {
    private String fullName;
    private Date dateOfBirth;
    private Gender gender;
    private String phoneNumber;
    private String address;
}