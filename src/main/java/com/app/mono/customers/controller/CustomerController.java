package com.app.mono.customers.controller;

import com.app.mono.common.ErrorResponseDto;
import com.app.mono.customers.dto.CustomerDetailsDto;
import com.app.mono.customers.service.CustomersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "XXBank 고객 REST API",
        description = "고객 상세 정보를 조회하는 REST API"
)
@RestController
@RequestMapping(path="/api", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class CustomerController {

    private final CustomersService customersService;

    public CustomerController(CustomersService customersService){
        this.customersService = customersService;
    }

    @Operation(
            summary = "고객 상세 정보 조회 API",
            description = "휴대폰 번호를 기준으로 고객, 계좌, 카드, 대출 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "요청 성공 (HTTP OK)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @GetMapping("/fetchCustomerDetails")
    public ResponseEntity<CustomerDetailsDto> fetchCustomerDetails(
            @RequestParam
            @Pattern(regexp="(^$|[0-9]{11})",message = "휴대폰 번호는 11자리 숫자여야 합니다.")
            String mobileNumber){

        CustomerDetailsDto customerDetailsDto =
                customersService.fetchCustomerDetails(mobileNumber);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customerDetailsDto);
    }

}