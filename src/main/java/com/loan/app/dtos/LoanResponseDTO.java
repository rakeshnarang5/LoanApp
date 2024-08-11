package com.loan.app.dtos;

import com.loan.app.entities.Loan;

import java.io.Serializable;

public record LoanResponseDTO(LoanRequestDTO loanRequest, Loan loan) implements Serializable {}
