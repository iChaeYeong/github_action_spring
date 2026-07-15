package com.app.mono.loans.controller;

import com.app.mono.common.ErrorResponseDto;
import com.app.mono.common.ResponseDto;
import com.app.mono.loans.constants.LoansConstants;
import com.app.mono.loans.dto.LoansDto;
import com.app.mono.loans.service.LoansService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "XXBank 대출 CRUD REST API",
        description = "XXBank 대출 정보를 생성/수정/조회/삭제(CRUD)하기 위한 REST API"
)
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/api", produces = {MediaType.APPLICATION_JSON_VALUE})
public class LoansController {

    private final LoansService loansService;

    @Operation(
            summary = "대출 생성 REST API",
            description = "XXBank 시스템에 신규 대출 정보를 생성하는 REST API"
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
    @PostMapping("/loans")
    public ResponseEntity<ResponseDto> createLoan(
            @RequestParam
            @Pattern(regexp = "(^$|[0-9]{11})", message = "휴대폰 번호는 11자리 숫자여야 합니다")
            String mobileNumber
    ) {
        loansService.createLoan(mobileNumber);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(LoansConstants.STATUS_201, LoansConstants.MESSAGE_201));
    }

    @Operation(
            summary = "대출 상세 조회 REST API",
            description = "휴대폰 번호를 기준으로 대출 상세 정보를 조회하는 REST API"
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
    @GetMapping("/loans")
    public ResponseEntity<LoansDto> fetchLoanDetails(
            @RequestParam
            @Pattern(regexp = "(^$|[0-9]{11})", message = "휴대폰 번호는 11자리 숫자여야 합니다")
            String mobileNumber
    ) {
        LoansDto loansDto = loansService.fetchLoan(mobileNumber);
        return ResponseEntity.status(HttpStatus.OK).body(loansDto);
    }

    @Operation(
            summary = "대출 정보 수정 REST API",
            description = "대출 번호를 기준으로 대출 정보를 수정하는 REST API"
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
    @PutMapping("/loans")
    public ResponseEntity<ResponseDto> updateLoanDetails(@Valid @RequestBody LoansDto loansDto) {
        boolean isUpdated = loansService.updateLoan(loansDto);

        if (isUpdated) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(LoansConstants.STATUS_200, LoansConstants.MESSAGE_200));
        } else {
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(new ResponseDto(LoansConstants.STATUS_417, LoansConstants.MESSAGE_417_UPDATE));
        }
    }

    @Operation(
            summary = "대출 삭제 REST API",
            description = "휴대폰 번호를 기준으로 대출 정보를 삭제하는 REST API"
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
    @DeleteMapping("/loans")
    public ResponseEntity<ResponseDto> deleteLoanDetails(
            @RequestParam
            @Pattern(regexp = "(^$|[0-9]{11})", message = "휴대폰 번호는 11자리 숫자여야 합니다")
            String mobileNumber
    ) {
        boolean isDeleted = loansService.deleteLoan(mobileNumber);

        if (isDeleted) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(LoansConstants.STATUS_200, LoansConstants.MESSAGE_200));
        } else {
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(new ResponseDto(LoansConstants.STATUS_417, LoansConstants.MESSAGE_417_DELETE));
        }
    }

}