package com.app.mono.cards.repository;

import com.app.mono.cards.entity.Cards;
import com.app.mono.customers.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface CardsRepository extends JpaRepository<Cards, Long> {

    Optional<Cards> findByCustomer(Customer customer);
    Optional<Cards> findByCardNumber(String cardNumber);

}