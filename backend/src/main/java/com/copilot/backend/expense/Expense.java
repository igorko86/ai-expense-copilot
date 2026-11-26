package com.copilot.backend.expense;

import com.copilot.backend.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String category;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Порожній конструктор обов'язковий для JPA (Hibernate)
    public Expense() {
    }
    @ManyToOne @JoinColumn(name = "user_id") private User user;

    // Конструктор для зручного створення об'єкта в коді
    public Expense(String description, BigDecimal amount, String currency, String category, LocalDate expenseDate, User user) {
        this.description = description;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.expenseDate = expenseDate;
        this.user = user;
    }

    // Getters & Setters (потрібні для серіалізації в JSON при відповіді на фронтенд)
    public Long getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getCategory() { return category; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
