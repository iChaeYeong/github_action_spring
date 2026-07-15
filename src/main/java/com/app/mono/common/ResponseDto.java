package com.app.mono.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(
        name = "응답정보",
        description = "API 성공 응답 정보를 담는 DTO"
)
public class ResponseDto {

    @Schema(
            description = "응답 상태 코드",
            example = "200"
    )
    private String statusCode;

    @Schema(
            description = "응답 메시지",
            example = "요청이 정상적으로 처리되었습니다."
    )
    private String statusMsg;
}