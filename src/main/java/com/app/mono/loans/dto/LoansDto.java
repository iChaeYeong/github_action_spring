package com.app.mono.loans.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Schema(
        name = "대출(Loans)",
        description = "대출 정보를 담기 위한 스키마"
)
@Data
public class LoansDto {

    @NotEmpty(message = "휴대폰 번호는 null 이거나 비어 있을 수 없습니다")
    @Pattern(regexp="(^$|[0-9]{11})", message = "휴대폰 번호는 11자리 숫자여야 합니다")
    @Schema(
            description = "고객의 휴대폰 번호", example = "01012341234"
    )
    private String mobileNumber;

    @NotEmpty(message = "대출 번호는 null 이거나 비어 있을 수 없습니다")
    @Pattern(regexp="(^$|[0-9]{12})", message = "대출 번호는 12자리 숫자여야 합니다")
    @Schema(
            description = "고객의 대출 번호", example = "548732457654"
    )
    private String loanNumber;

    @NotEmpty(message = "대출 유형은 null 이거나 비어 있을 수 없습니다")
    @Schema(
            description = "대출 종류", example = "주택담보대출"
    )
    private String loanType;

    @Positive(message = "총 대출 금액은 0보다 커야 합니다")
    @Schema(
            description = "총 대출 금액", example = "100000"
    )
    private int totalLoan;

    @PositiveOrZero(message = "상환 금액은 0 이상이어야 합니다")
    @Schema(
            description = "현재까지 상환된 총 금액", example = "1000"
    )
    private int amountPaid;

    @PositiveOrZero(message = "남은 대출 금액은 0 이상이어야 합니다")
    @Schema(
            description = "남아있는 대출 잔액", example = "99000"
    )
    private int outstandingAmount;

}