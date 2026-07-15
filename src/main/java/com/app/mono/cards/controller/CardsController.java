package com.app.mono.cards.controller;

import com.app.mono.cards.constants.CardsConstants;
import com.app.mono.cards.dto.CardsDto;
import com.app.mono.cards.service.CardsService;
import com.app.mono.common.ErrorResponseDto;
import com.app.mono.common.ResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "XXBank 카드 CRUD REST API",
        description = "XXBank 카드 정보를 생성/수정/조회/삭제(CRUD)하기 위한 REST API"
)
@RestController
@RequestMapping(path = "/api", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
@Validated
public class CardsController {

    private final  CardsService cardsService;

    @Operation(
            summary = "카드 생성 REST API",
            description = "XXBank 시스템에 신규 카드를 생성하는 REST API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "HTTP 상태: 생성됨(CREATED)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP 상태: 내부 서버 오류(Internal Server Error)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PostMapping("/cards")
    public ResponseEntity<ResponseDto> createCard(
            @Valid @RequestParam
            @Pattern(regexp = "(^$|[0-9]{11})", message = "휴대폰 번호는 11자리 숫자여야 합니다")
            String mobileNumber
    ) {
        cardsService.createCard(mobileNumber);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(CardsConstants.STATUS_201, CardsConstants.MESSAGE_201));
    }

    @Operation(
            summary = "카드 상세 조회 REST API",
            description = "휴대폰 번호를 기준으로 카드 상세 정보를 조회하는 REST API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP 상태: 성공(OK)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP 상태: 내부 서버 오류(Internal Server Error)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @GetMapping("/cards")
    public ResponseEntity<CardsDto> fetchCardDetails(
            @RequestParam
            @Pattern(regexp = "(^$|[0-9]{11})", message = "휴대폰 번호는 11자리 숫자여야 합니다")
            String mobileNumber
    ) {
        CardsDto cardsDto = cardsService.fetchCard(mobileNumber);
        return ResponseEntity.status(HttpStatus.OK).body(cardsDto);
    }

    @Operation(
            summary = "카드 정보 수정 REST API",
            description = "카드 번호를 기준으로 카드 정보를 수정하는 REST API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP 상태: 성공(OK)"
            ),
            @ApiResponse(
                    responseCode = "417",
                    description = "HTTP 상태: 기대 실패(Expectation Failed)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP 상태: 내부 서버 오류(Internal Server Error)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PutMapping("/cards")
    public ResponseEntity<ResponseDto> updateCardDetails(@Valid @RequestBody CardsDto cardsDto) {
        boolean isUpdated = cardsService.updateCard(cardsDto);

        if (isUpdated) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(CardsConstants.STATUS_200, CardsConstants.MESSAGE_200));
        } else {
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(new ResponseDto(CardsConstants.STATUS_417, CardsConstants.MESSAGE_417_UPDATE));
        }
    }

    @Operation(
            summary = "카드 삭제 REST API",
            description = "휴대폰 번호를 기준으로 카드 정보를 삭제하는 REST API"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP 상태: 성공(OK)"
            ),
            @ApiResponse(
                    responseCode = "417",
                    description = "HTTP 상태: 기대 실패(Expectation Failed)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP 상태: 내부 서버 오류(Internal Server Error)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @DeleteMapping("/cards")
    public ResponseEntity<ResponseDto> deleteCardDetails(
            @RequestParam
            @Pattern(regexp = "(^$|[0-9]{11})", message = "휴대폰 번호는 11자리 숫자여야 합니다")
            String mobileNumber
    ) {
        boolean isDeleted = cardsService.deleteCard(mobileNumber);

        if (isDeleted) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(CardsConstants.STATUS_200, CardsConstants.MESSAGE_200));
        } else {
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(new ResponseDto(CardsConstants.STATUS_417, CardsConstants.MESSAGE_417_DELETE));
        }
    }

}