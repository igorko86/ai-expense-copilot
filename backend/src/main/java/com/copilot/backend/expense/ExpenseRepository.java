package com.copilot.backend.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // Spring Data JPA сам зрозуміє за назвою методу, що треба зробити:
    // SELECT * FROM expenses WHERE category = ?
    List<Expense> findByCategoryIgnoreCase(String category);
    List<Expense> findAllByUserId(Long userId);
    List<Expense> findByUserIdAndCategoryIgnoreCase(Long userId, String category);

    List<Expense> findAll();

    Expense save(Expense expense);

    // Новий метод: шукає всі витрати, у назві яких є потрібне слово (без урахування регістру)
    List<Expense> findByUserIdAndDescriptionContainingIgnoreCase(Long id, String keyword);
}



