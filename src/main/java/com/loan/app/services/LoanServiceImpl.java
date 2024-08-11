package com.loan.app.services;

import com.loan.app.dtos.LoanRequestDTO;
import com.loan.app.dtos.LoanResponseDTO;
import com.loan.app.entities.Loan;
import com.loan.app.entities.ScheduledPayment;
import com.loan.app.entities.User;
import com.loan.app.enums.LoanStatus;
import com.loan.app.enums.PaymentStatus;
import com.loan.app.exceptions.LoanException;
import com.loan.app.repositories.LoanRepository;
import com.loan.app.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    private final UserRepository userRepository;

    public LoanServiceImpl(LoanRepository loanRepository, UserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    @Override
    public LoanResponseDTO createLoan(LoanRequestDTO loanRequest) {
        User loanRequester = userRepository.findByUsername("rohan");
        if (loanRequester == null) {
            throw new LoanException("Invalid User: " + "rohan");
        }
        validateLoanRequest(loanRequest);
        Loan loan = new Loan(loanRequest.amount(), loanRequest.term(), loanRequester, LoanStatus.PENDING);
        int ewi = loanRequest.amount() / loanRequest.term();
        int roundingError = loanRequest.amount() - (ewi * loanRequest.term());
        LocalDate currDate = LocalDate.now();
        for (int i = 0; i < loanRequest.term(); i++) {
            LocalDate ewiDate = currDate.plusWeeks(1);
            if (i == loanRequest.term() - 1) {
                loan.addScheduledPayment(new ScheduledPayment(ewi + roundingError, ewiDate, PaymentStatus.PENDING));
                continue;
            }
            loan.addScheduledPayment(new ScheduledPayment(ewi, ewiDate, PaymentStatus.PENDING));
            currDate = ewiDate;
        }
        loanRepository.save(loan);
        return new LoanResponseDTO(loanRequest, loan);
    }

    @Override
    public List<Loan> getPendingLoans() {
        return loanRepository.findPendingLoans(userRepository.findByUsername("alpha"));
    }

    @Override
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Override
    public Loan approveLoan(int loanId) {
        return loanRepository.approveLoan(loanId);
    }

    @Override
    public Loan repay(int loanId, int amount) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) {
            throw new LoanException("Loan with this id doesn't exist: " + loanId);
        }
        if (loan.getStatus() == LoanStatus.PENDING) {
            throw new LoanException("Loan is in pending state");
        }
        if (loan.getStatus() == LoanStatus.PAID) {
            throw new LoanException("Loan is already paid off.");
        }
        ScheduledPayment scheduledPayment = loan.getScheduledPayments().stream()
                .filter(payment -> payment.getPaymentStatus() == PaymentStatus.PENDING)
                .findFirst().orElse(null);
        if (scheduledPayment == null) {
            throw new LoanException("No payment pending for loan: " + loanId);
        }
        if (scheduledPayment.getPaymentAmount() > amount) {
            throw new LoanException("Minimum payment amount is: " + amount);
        }
        scheduledPayment.setPaymentStatus(PaymentStatus.PAID);
        scheduledPayment.setPaidOnDate(LocalDate.now());
        if (loan.getScheduledPayments().stream().allMatch(payment -> payment.getPaymentStatus() == PaymentStatus.PAID)) {
            loan.setStatus(LoanStatus.PAID);
        }
        int residualAmount = amount - scheduledPayment.getPaymentAmount();
        if (residualAmount != 0) {
            loan.setResidualAmount(loan.getResidualAmount() + residualAmount);
        }
        return loan;
    }

    private void validateLoanRequest(LoanRequestDTO loanRequest) {
        if (loanRequest.amount() < 100) {
            throw new LoanException("Minimum Loan Amount is $100");
        }
        if (loanRequest.term() < 1) {
            throw new LoanException("Minimum Loan Term is 1w");
        }
    }

}
