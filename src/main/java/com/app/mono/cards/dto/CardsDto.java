package com.app.mono.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Schema(
        name = "카드",
        description = "카드 정보를 담기 위한 스키마"
)
@Data
public class CardsDto {

    @NotEmpty(message = "휴대폰 번호는 null 이거나 비어 있을 수 없습니다")
    @Pattern(regexp="(^$|[0-9]{11})", message = "휴대폰 번호는 11자리 숫자여야 합니다")
    @Schema(
            description = "고객의 휴대폰 번호", example = "01012341234"
    )
    private String mobileNumber;

    @NotEmpty(message = "카드 번호는 null 이거나 비어 있을 수 없습니다")
    @Pattern(regexp="(^$|[0-9]{12})", message = "카드 번호는 12자리 숫자여야 합니다")
    @Schema(
            description = "고객의 카드 번호", example = "100646930341"
    )
    private String cardNumber;

    @NotEmpty(message = "카드 유형은 null 이거나 비어 있을 수 없습니다")
    @Schema(
            description = "카드 종류", example = "신용카드"
    )
    private String cardType;

    @Positive(message = "총 카드 한도는 0보다 커야 합니다")
    @Schema(
            description = "카드의 총 사용 한도 금액", example = "100000"
    )
    private int totalLimit;

    @PositiveOrZero(message = "사용 금액은 0 이상이어야 합니다")
    @Schema(
            description = "고객이 사용한 총 금액", example = "1000"
    )
    private int amountUsed;

    @PositiveOrZero(message = "사용 가능 금액은 0 이상이어야 합니다")
    @Schema(
            description = "카드에서 사용 가능한 남은 금액", example = "90000"
    )
    private int availableAmount;

}