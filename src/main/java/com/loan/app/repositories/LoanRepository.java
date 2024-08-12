package com.loan.app.repositories;

import com.loan.app.entities.Loan;

import java.util.List;

public interface LoanRepository {
    List<Loan> findAll();

    Loan findById(int loanId);

    List<Loan> findLoansByUsername(String username);

    List<Loan> findPendingLoans();

    void save(Loan loan);

    Loan approveLoan(int loanId);
}
