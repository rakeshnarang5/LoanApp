package com.loan.app.services;

import com.loan.app.dtos.LoanRequestDTO;
import com.loan.app.dtos.LoanResponseDTO;
import com.loan.app.entities.Loan;

import java.util.List;

public interface LoanService {

    LoanResponseDTO createLoan(LoanRequestDTO loanRequest, String username);

    List<Loan> getPendingLoans();

    List<Loan> getAllLoans();

    Loan approveLoan(int loanId);

    Loan repay(int loanId, int amount, String username);

    List<Loan> getUserLoans(String username);
}
