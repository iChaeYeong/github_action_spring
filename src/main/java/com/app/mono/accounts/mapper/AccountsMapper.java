package com.app.mono.accounts.mapper;

import com.app.mono.accounts.dto.AccountsDto;
import com.app.mono.accounts.entity.Accounts;
import com.app.mono.customers.entity.Customer;

public class AccountsMapper {

    // Entity -> DTO
    public static AccountsDto mapToAccountsDto(Accounts accounts, AccountsDto accountsDto) {
        if (accounts == null) return null;
        if (accountsDto == null) accountsDto = new AccountsDto();

        accountsDto.setAccountNumber(accounts.getAccountNumber());
        accountsDto.setAccountType(accounts.getAccountType());
        accountsDto.setBranchAddress(accounts.getBranchAddress());

        return accountsDto;
    }

    // DTO -> Entity
    public static Accounts mapToAccounts(AccountsDto accountsDto, Accounts accounts) {
        if (accountsDto == null) return null;
        if (accounts == null) accounts = new Accounts();

        accounts.setAccountNumber(accountsDto.getAccountNumber());
        accounts.setAccountType(accountsDto.getAccountType());
        accounts.setBranchAddress(accountsDto.getBranchAddress());

        return accounts;
    }
//    public static AccountsDto mapToAccountsDto(Accounts accounts, AccountsDto accountsDto) {
//        accountsDto.setAccountNumber(accounts.getAccountNumber());
//        accountsDto.setAccountType(accounts.getAccountType());
//        accountsDto.setBranchAddress(accounts.getBranchAddress());
//        return accountsDto;
//    }
//
//    public static Accounts mapToAccounts(AccountsDto accountsDto, Accounts accounts) {
//        accounts.setAccountNumber(accountsDto.getAccountNumber());
//        accounts.setAccountType(accountsDto.getAccountType());
//        accounts.setBranchAddress(accountsDto.getBranchAddress());
//        return accounts;
//    }
}