package com.app.mono.accounts.repository;

import com.app.mono.accounts.entity.Accounts;
import com.app.mono.customers.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountsRepository extends JpaRepository<Accounts, Long> {

    Optional<Accounts> findByCustomer(Customer customer);

    void deleteByCustomer(Customer customer);

}
