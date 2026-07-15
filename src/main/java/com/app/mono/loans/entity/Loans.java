package com.app.mono.loans.entity;

import com.app.mono.common.BaseEntity;
import com.app.mono.customers.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loans")
@Getter
@Setter
@ToString(exclude = "customer")
@AllArgsConstructor
@NoArgsConstructor
public class Loans extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    private String loanNumber;

    private String loanType;

    private int totalLoan;

    private int amountPaid;

    private int outstandingAmount;

    // ==============================
    // FK 관계 설정
    // ==============================
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}