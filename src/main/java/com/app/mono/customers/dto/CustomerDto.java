package com.app.mono.customers.dto;

import com.app.mono.accounts.dto.AccountsDto;
import com.app.mono.customers.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor @Builder
@ToString
@Schema(
        name = "고객정보",
        description = "고객 및 계좌 정보를 담는 DTO"
)
public class CustomerDto {

    @Schema(
            description = "고객 아이디",
            example = "inky4832"
    )
    private Long customerId;

    @Schema(
            description = "고객 이름",
            example = "이지바이트"
    )
    @NotEmpty(message = "고객 이름은 필수 입력값입니다.")
    @Size(min = 2, max = 5, message = "고객 이름 길이는 2자 이상 5자 이하입니다.")
    private String username;

    @Schema(
            description = "고객 이메일 주소",
            example = "tutor@google.com"
    )
    @NotEmpty(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

    @Schema(
            description = "고객 비밀번호",
            example = "PassW0rd!234"
    )
    private String password;

    @Builder.Default
    private Role role = Role.USER;

    @Schema(
            description = "고객 휴대폰 번호 (형식: 01012341234)",
            example = "01012341234"
    )
    @Pattern(
            regexp = "^010\\d{4}\\d{4}$",
            message = "휴대폰 번호는 01012341234 형식이어야 합니다."
    )
    private String mobileNumber;

    @Schema(
            description = "고객 계좌 정보"
    )
    private AccountsDto accountsDto;
}