package com.app.mono.customers.service;

import com.app.mono.customers.dto.CustomerDetailsDto;
import com.app.mono.customers.dto.CustomerDto;
import com.app.mono.customers.dto.LoginDTO;

public interface CustomersService {
    CustomerDetailsDto fetchCustomerDetails(String mobileNumber);
    public CustomerDto login(LoginDTO loginDto);
}
