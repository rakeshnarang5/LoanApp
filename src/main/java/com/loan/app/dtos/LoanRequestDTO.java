package com.loan.app.dtos;

import java.io.Serializable;

public record LoanRequestDTO(int amount, int term) implements Serializable {}
