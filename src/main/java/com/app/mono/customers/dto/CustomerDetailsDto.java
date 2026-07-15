package com.app.mono.customers.dto;

import com.app.mono.accounts.dto.AccountsDto;
import com.app.mono.cards.dto.CardsDto;
import com.app.mono.loans.dto.LoansDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(
        name = "고객상세정보",
        description = "고객, 계좌, 카드, 대출 정보를 포함하는 DTO"
)
public class CustomerDetailsDto {

    @Schema(
            description = "고객 이름",
            example = "홍길동"
    )
    @NotEmpty(message = "이름은 비어 있을 수 없습니다.")
    @Size(min = 5, max = 30, message = "고객 이름은 5자 이상 30자 이하로 입력해야 합니다.")
    private String username;

    @Schema(
            description = "고객 이메일 주소",
            example = "test@example.com"
    )
    @NotEmpty(message = "이메일은 비어 있을 수 없습니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

    @Schema(
            description = "고객 휴대폰 번호 (11자리 숫자)",
            example = "01012345678"
    )
    @Pattern(regexp = "(^$|[0-9]{11})", message = "휴대폰 번호는 11자리 숫자여야 합니다.")
    private String mobileNumber;

    @Schema(
            description = "고객 계좌 정보"
    )
    private AccountsDto accountsDto;

    @Schema(
            description = "고객 대출 정보"
    )
    private LoansDto loansDto;

    @Schema(
            description = "고객 카드 정보"
    )
    private CardsDto cardsDto;

}