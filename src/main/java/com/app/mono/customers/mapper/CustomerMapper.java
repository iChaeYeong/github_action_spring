package com.app.mono.customers.mapper;

import com.app.mono.customers.dto.CustomerDetailsDto;
import com.app.mono.customers.dto.CustomerDto;
import com.app.mono.customers.entity.Customer;

public class CustomerMapper {
//    public static CustomerDto mapToCustomerDto(Customer customer, CustomerDto customerDto) {
//        if (customer == null) return null;
//        if (customerDto == null) customerDto = new CustomerDto();
//
//        customerDto.setCustomerId(customer.getCustomerId());
//        customerDto.setUsername(customer.getUsername());
//        customerDto.setEmail(customer.getEmail());
//        customerDto.setMobileNumber(customer.getMobileNumber());
//        customerDto.setRole(customer.getRole());
//
//        // 보안상 password는 내려주지 않음
//        customerDto.setPassword(null);
//
//        // accountsDto는 여기서 매핑하지 않음 (별도 조립 권장)
//        // customerDto.setAccountsDto(...);
//
//        return customerDto;
//    }
//
//    public static Customer mapToCustomer(CustomerDto customerDto, Customer customer) {
//        if (customerDto == null) return null;
//        if (customer == null) customer = new Customer();
//
//        // PK는 신규 생성 시엔 세팅하지 않는 편이 안전하지만,
//        // update 시 필요할 수 있어 그대로 유지
////        customer.setCustomerId(customerDto.getCustomerId());
//
//        customer.setUsername(customerDto.getUsername());
//        customer.setEmail(customerDto.getEmail());
//        customer.setMobileNumber(customerDto.getMobileNumber());
//
//        // role이 null이면 엔티티 기본값 유지(또는 USER로 강제)
//        if (customerDto.getRole() != null) {
//            customer.setRole(customerDto.getRole());
//        }
//
//        // password는 DTO에 값이 있을 때만 갱신 (update 시 빈 값 덮어쓰기 방지)
//        if (customerDto.getPassword() != null && !customerDto.getPassword().isBlank()) {
//            customer.setPassword(customerDto.getPassword()); // 보통 서비스에서 encoder 적용
//        }
//
//        // accounts / cards / loans 컬렉션은 여기서 건드리지 않음 (연관관계는 별도 로직)
//        return customer;
//    }
//
//    public static CustomerDetailsDto mapToCustomerDetailsDto(Customer customer, CustomerDetailsDto customerDetailsDto) {
//        if (customer == null) return null;
//        if (customerDetailsDto == null) customerDetailsDto = new CustomerDetailsDto();
//
//        customerDetailsDto.setUsername(customer.getUsername());
//        customerDetailsDto.setEmail(customer.getEmail());
//        customerDetailsDto.setMobileNumber(customer.getMobileNumber());
//
//        return customerDetailsDto;
//    }

    public static CustomerDto mapToCustomerDto(Customer customer, CustomerDto customerDto) {
        customerDto.setUsername(customer.getUsername());
        customerDto.setEmail(customer.getEmail());
        customerDto.setMobileNumber(customer.getMobileNumber());
        customerDto.setCustomerId(customer.getCustomerId());
        return customerDto;
    }

    public static Customer mapToCustomer(CustomerDto customerDto, Customer customer) {
        customer.setUsername(customerDto.getUsername());
        // password는 DTO에 값이 있을 때만 갱신 (update 시 빈 값 덮어쓰기 방지)
        if (customerDto.getPassword() != null && !customerDto.getPassword().isBlank()) {
            customer.setPassword(customerDto.getPassword()); // 보통 서비스에서 encoder 적용
        }
        customer.setEmail(customerDto.getEmail());
        customer.setMobileNumber(customerDto.getMobileNumber());
        return customer;
    }

    public static CustomerDetailsDto mapToCustomerDetailsDto(Customer customer, CustomerDetailsDto customerDetailsDto) {
        customerDetailsDto.setUsername(customer.getUsername());
        customerDetailsDto.setEmail(customer.getEmail());
        customerDetailsDto.setMobileNumber(customer.getMobileNumber());
        return customerDetailsDto;
    }
}