package com.app.mono.accounts.entity;

import com.app.mono.common.BaseEntity;
import com.app.mono.customers.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@ToString(exclude = "customer")
//@ToString()
@AllArgsConstructor
@NoArgsConstructor
public class Accounts extends BaseEntity {

    @Id
    @Column(name = "account_number")
    private Long accountNumber;

    private String accountType;

    private String branchAddress;

    // ==============================
    // FK 관계 설정
    // ==============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
