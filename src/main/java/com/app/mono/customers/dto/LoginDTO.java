// LoginDTO.java
package com.app.mono.customers.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 로그인 요청 DTO
 * 
 * 요청 예시:
 * {
 *   "username": "testuser",
 *   "password": "12345678"
 * }
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class LoginDTO {

    private String email;
    private String password;
}