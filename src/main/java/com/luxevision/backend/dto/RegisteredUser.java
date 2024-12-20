package com.luxevision.backend.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisteredUser implements Serializable {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String jwt;
}
