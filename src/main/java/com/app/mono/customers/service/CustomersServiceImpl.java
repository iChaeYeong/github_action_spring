package com.app.mono.customers.service;

import com.app.mono.accounts.dto.AccountsDto;
import com.app.mono.accounts.entity.Accounts;
import com.app.mono.accounts.mapper.AccountsMapper;
import com.app.mono.accounts.repository.AccountsRepository;
import com.app.mono.cards.dto.CardsDto;
import com.app.mono.cards.entity.Cards;
import com.app.mono.cards.mapper.CardsMapper;
import com.app.mono.cards.repository.CardsRepository;
import com.app.mono.common.exception.ResourceNotFoundException;
import com.app.mono.customers.dto.CustomerDetailsDto;
import com.app.mono.customers.dto.CustomerDto;
import com.app.mono.customers.dto.LoginDTO;
import com.app.mono.customers.entity.Customer;
import com.app.mono.customers.mapper.CustomerMapper;
import com.app.mono.customers.repository.CustomerRepository;
import com.app.mono.loans.dto.LoansDto;
import com.app.mono.loans.entity.Loans;
import com.app.mono.loans.mapper.LoansMapper;
import com.app.mono.loans.repository.LoansRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomersServiceImpl implements CustomersService{

    private final AccountsRepository accountsRepository;
    private final CustomerRepository customerRepository;
    private final LoansRepository loansRepository;
    private final CardsRepository cardsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomer(customer).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );

        Cards cards = cardsRepository.findByCustomer(customer).orElseThrow(
                () -> new ResourceNotFoundException("Cards", "customerId", customer.getCustomerId().toString())
        );

        Loans loans = loansRepository.findByCustomer(customer).orElseThrow(
                () -> new ResourceNotFoundException("Loans", "customerId", customer.getCustomerId().toString())
        );


        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());

        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));
        customerDetailsDto.setCardsDto(CardsMapper.mapToCardsDto(cards, new CardsDto()));
        customerDetailsDto.setLoansDto(LoansMapper.mapToLoansDto(loans, new LoansDto()));
        return customerDetailsDto;
    }

    /**
     * ✅ 로그인(현재 구현은 "직접 검증" 방식)
     *
     * 흐름:
     * 1) email로 사용자 조회
     * 2) passwordEncoder.matches(raw, encoded) 로 비밀번호 검증
     * 3) 성공하면 MemberDTO 반환
     *
     * ⚠️ 그런데 현재 프로젝트에서는 AuthController에서
     * authenticationManager.authenticate()를 이미 수행 중이므로
     * 이 메서드는 중복 검증이 된다.
     *
     * ✅ 권장:
     * - 여기서 비밀번호 검증을 빼고,
     *   사용자 조회 + DTO 변환만 하도록 변경하는 편이 좋다.
     */
    @Override
    public CustomerDto login(LoginDTO dto) {

        Customer entity = customerRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        return CustomerDto.builder()
                .customerId(entity.getCustomerId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .role(entity.getRole())
                .build();
    }
}
