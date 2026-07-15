package com.app.mono.accounts.dto;

import com.app.mono.customers.dto.CustomerDto;
import com.app.mono.customers.entity.Customer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(
        name = "계좌정보",
        description = "계좌 정보를 담는 DTO"
)
public class AccountsDto {

    @NotNull(message = "계좌번호는 필수 입력값입니다.")
    @Schema(
            description = "XX뱅크 계좌번호 (10자리 숫자)",
            example = "3454433243"
    )
    private Long accountNumber;

    @NotEmpty(message = "계좌 타입은 필수 입력값입니다.")
    @Schema(
            description = "계좌 유형 (예: Savings, Checking)",
            example = "Savings"
    )
    private String accountType;

    @NotEmpty(message = "지점 주소는 필수 입력값입니다.")
    @Schema(
            description = "XX뱅크 지점 주소",
            example = "서울 강남구 테헤란로 123"
    )
    private String branchAddress;

}

