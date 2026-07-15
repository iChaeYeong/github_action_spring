package com.app.mono.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "에러응답",
        description = "API 오류 발생 시 반환되는 응답 DTO"
)
public class ErrorResponseDto {

    @Schema(
            description = "클라이언트가 호출한 API 경로",
            example = "/api/createAccount"
    )
    private String apiPath;

    @Schema(
            description = "에러 상태 코드",
            example = "400"
    )
    private int errorCode;

    @Schema(
            description = "에러 메시지",
            example = "잘못된 요청입니다."
    )
    private String errorMessage;

    @Schema(
            description = "에러 발생 시간",
            example = "2026-02-18T10:15:30"
    )
    private LocalDateTime errorTime;
}