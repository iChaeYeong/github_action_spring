package com.app.mono.loans.service;

import com.app.mono.common.exception.ResourceNotFoundException;
import com.app.mono.customers.entity.Customer;
import com.app.mono.customers.repository.CustomerRepository;
import com.app.mono.loans.constants.LoansConstants;
import com.app.mono.loans.dto.LoansDto;
import com.app.mono.loans.entity.Loans;
import com.app.mono.loans.exception.LoanAlreadyExistsException;
import com.app.mono.loans.mapper.LoansMapper;
import com.app.mono.loans.repository.LoansRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class LoansServiceImpl implements LoansService {

    private final LoansRepository loansRepository;
    private final CustomerRepository customerRepository;


    @Override
    @Transactional
    public void createLoan(String mobileNumber) {

        // 휴대폰 번호로 고객 조회 (없으면 예외)
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("고객", "휴대폰 번호", mobileNumber)
        );

        Optional<Loans> optionalLoans = loansRepository.findByCustomer(customer);

        // 이미 해당 휴대폰 번호로 대출이 등록되어 있으면 예외 발생
        if (optionalLoans.isPresent()) {
            throw new LoanAlreadyExistsException("해당 휴대폰 번호로 이미 대출이 등록되어 있습니다: " + mobileNumber);
        }

        // 새 대출 생성 후 저장
        loansRepository.save(createNewLoan(mobileNumber, customer));
    }

    private Loans createNewLoan(String mobileNumber, Customer customer) {
        Loans newLoan = new Loans();

        // 임의 대출번호 생성
        long randomLoanNumber = 100000000000L + new Random().nextInt(900000000);
        newLoan.setLoanNumber(Long.toString(randomLoanNumber));

        // 대출 기본 정보 세팅
        newLoan.setCustomer(customer);
        newLoan.setLoanType(LoansConstants.HOME_LOAN);

        // 총 대출금/상환금/잔액 세팅
        newLoan.setTotalLoan(LoansConstants.NEW_LOAN_LIMIT);
        newLoan.setAmountPaid(0);
        newLoan.setOutstandingAmount(LoansConstants.NEW_LOAN_LIMIT);

        return newLoan;
    }

    @Override
    public LoansDto fetchLoan(String mobileNumber) {

        // 휴대폰 번호로 고객 조회 (없으면 예외)
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("고객", "휴대폰 번호", mobileNumber)
        );

        // 휴대폰 번호로 대출 조회, 없으면 예외 발생
        Loans loans = loansRepository.findByCustomer(customer).orElseThrow(
                () -> new ResourceNotFoundException("대출", "휴대폰 번호", mobileNumber)
        );

        // Entity -> DTO 변환
        return LoansMapper.mapToLoansDto(loans, new LoansDto());
    }

    @Override
    public boolean updateLoan(LoansDto loansDto) {
        // 대출번호로 대출 조회, 없으면 예외 발생
        Loans loans = loansRepository.findByLoanNumber(loansDto.getLoanNumber()).orElseThrow(
                () -> new ResourceNotFoundException("대출", "대출번호", loansDto.getLoanNumber())
        );

        // DTO -> Entity 매핑 후 저장
        LoansMapper.mapToLoans(loansDto, loans);
        loansRepository.save(loans);

        return true;
    }

    @Override
    public boolean deleteLoan(String mobileNumber) {

        // 휴대폰 번호로 고객 조회 (없으면 예외)
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("고객", "휴대폰 번호", mobileNumber)
        );

        // 휴대폰 번호로 대출 조회, 없으면 예외 발생
        Loans loans = loansRepository.findByCustomer(customer).orElseThrow(
                () -> new ResourceNotFoundException("대출", "휴대폰 번호", mobileNumber)
        );

        // 대출 삭제
        loansRepository.deleteById(loans.getLoanId());

        return true;
    }
}