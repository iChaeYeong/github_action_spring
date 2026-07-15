package com.app.mono.customers.entity;

import com.app.mono.accounts.entity.Accounts;
import com.app.mono.cards.entity.Cards;
import com.app.mono.common.BaseEntity;
import com.app.mono.customers.entity.Role;
import com.app.mono.loans.entity.Loans;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "customers")
@Getter
@Setter
//@ToString(exclude = {"accounts", "cards", "loans"})
@ToString()
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    private String username;

    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "mobile_number")
    private String mobileNumber;

    // ==============================
    // 관계 설정
    // ==============================

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Accounts> accounts;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cards> cards;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loans> loans;
}