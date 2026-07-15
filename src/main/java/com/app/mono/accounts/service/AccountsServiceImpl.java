package com.app.mono.accounts.service;

import com.app.mono.accounts.constants.AccountsConstants;
import com.app.mono.accounts.dto.AccountsDto;
import com.app.mono.accounts.entity.Accounts;
import com.app.mono.accounts.exception.CustomerAlreadyExistsException;
import com.app.mono.accounts.mapper.AccountsMapper;
import com.app.mono.accounts.repository.AccountsRepository;
import com.app.mono.common.exception.ResourceNotFoundException;
import com.app.mono.customers.dto.CustomerDto;
import com.app.mono.customers.entity.Customer;
import com.app.mono.customers.mapper.CustomerMapper;
import com.app.mono.customers.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountsServiceImpl implements AccountsService {

    private final AccountsRepository accountsRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void createAccount(CustomerDto customerDto) {
        // DTO -> Entity 변환
        customerDto.setPassword(passwordEncoder.encode(customerDto.getPassword())); // 비번 암호화
        Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());

        // 휴대폰 번호로 기존 고객 존재 여부 확인
        Optional<Customer> optionalCustomer = customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if (optionalCustomer.isPresent()) {
            throw new CustomerAlreadyExistsException(
                    "해당 휴대폰 번호로 이미 등록된 고객이 존재합니다. 휴대폰 번호: " + customerDto.getMobileNumber()
            );
        }

        // 고객 저장 후, 해당 고객에 대한 신규 계좌 생성/저장
        Customer savedCustomer = customerRepository.save(customer);
        accountsRepository.save(createNewAccount(savedCustomer));
    }

    private Accounts createNewAccount(Customer customer) {
        Accounts newAccount = new Accounts();

        // 고객 ID 매핑
        newAccount.setCustomer(customer);

        // 임의 계좌번호 생성 (10자리 근사)
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);
        newAccount.setAccountNumber(randomAccNumber);

        // 기본 계좌 정보 세팅
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);

        return newAccount;
    }

    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        // 휴대폰 번호로 고객 조회 (없으면 예외)
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("고객", "휴대폰 번호", mobileNumber)
        );

        // 고객 ID로 계좌 조회 (없으면 예외)
        Accounts accounts = accountsRepository.findByCustomer(customer).orElseThrow(
                () -> new ResourceNotFoundException("계좌", "고객 ID", customer.getCustomerId().toString())
        );

        // Entity -> DTO 변환 후 조립
        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

        return customerDto;
    }

    @Override
    @Transactional
    public boolean updateAccount(CustomerDto customerDto) {
        boolean isUpdated = false;

        AccountsDto accountsDto = customerDto.getAccountsDto();
        if (accountsDto != null) {
            // 계좌번호로 계좌 조회 (없으면 예외)
            Accounts accounts = accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFoundException("계좌", "계좌번호", accountsDto.getAccountNumber().toString())
            );
            System.out.println(">>>>>>>>>>>>>>>>0" + customerDto);
            System.out.println(">>>>>>>>>>>>>>>>1" + accounts);
            // 계좌 업데이트
            AccountsMapper.mapToAccounts(accountsDto, accounts);
            accounts= accountsRepository.save(accounts);
            System.out.println(">>>>>>>>>>>>>>>>11" + accounts);
            System.out.println(">>>>>>>>>>>>>>>>12" + accountsDto);
            // 계좌에 연결된 고객 조회 후 업데이트
 System.out.println(">>>>>>>>>>>>>>>>2" + accounts.getCustomer());
            Long customerId = accounts.getCustomer().getCustomerId();
            Customer customer = customerRepository.findById(customerId).orElseThrow(
                    () -> new ResourceNotFoundException("고객", "고객 ID", customerId.toString())
            );
            System.out.println(">>>>>>>>>>>>>>>>98" + customerDto);
            System.out.println(">>>>>>>>>>>>>>>>99" + customer);
            CustomerMapper.mapToCustomer(customerDto, customer);
            System.out.println(">>>>>>>>>>>>>>>>100" + customerDto);
            System.out.println(">>>>>>>>>>>>>>>>101" + customer);
            customerRepository.save(customer);

            isUpdated = true;
        }

        return isUpdated;
    }

    @Override
    @Transactional
    public boolean deleteAccount(String mobileNumber) {
        // 휴대폰 번호로 고객 조회 (없으면 예외)
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("고객", "휴대폰 번호", mobileNumber)
        );

        // 고객 ID 기준으로 계좌 삭제 후 고객 삭제
        accountsRepository.deleteByCustomer(customer);
        customerRepository.deleteById(customer.getCustomerId());

        return true;
    }
}