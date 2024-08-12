package com.loan.app.services;

import com.loan.app.dtos.LoanRequestDTO;
import com.loan.app.dtos.LoanResponseDTO;
import com.loan.app.entities.Loan;
import com.loan.app.entities.ScheduledPayment;
import com.loan.app.enums.LoanStatus;
import com.loan.app.enums.PaymentStatus;
import com.loan.app.exceptions.LoanException;
import com.loan.app.repositories.LoanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    public LoanServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public LoanResponseDTO createLoan(LoanRequestDTO loanRequest, String username) {
        validateLoanRequest(loanRequest);
        Loan loan = getLoan(loanRequest, username);
        loanRepository.save(loan);
        return new LoanResponseDTO(loanRequest, loan);
    }

    private static Loan getLoan(LoanRequestDTO loanRequest, String username) {
        Loan loan = new Loan(loanRequest.amount(), loanRequest.term(), username, LoanStatus.PENDING);
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
        return loan;
    }

    @Override
    public List<Loan> getPendingLoans() {
        return loanRepository.findPendingLoans();
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
    public Loan repay(int loanId, int amount, String username) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) {
            throw new LoanException("Loan with this id doesn't exist: " + loanId);
        }
        if (!loan.getUser().equals(username)){
            throw new LoanException("Loan belongs to different user: "+loan.getUser());
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
        if (LocalDate.now().isAfter(scheduledPayment.getPaymentDueDate())){
            loan.setStatus(LoanStatus.DEFAULTED);
            throw new LoanException("Payment cannot be done after due date, loan is moved Defaulted state");
        }
        if (scheduledPayment.getPaymentAmount() > amount) {
            throw new LoanException("Minimum payment amount is: " + scheduledPayment.getPaymentAmount());
        }
        scheduledPayment.setPaymentStatus(PaymentStatus.PAID);
        scheduledPayment.setPaidOnDate(LocalDate.now());
        if (loan.getScheduledPayments().stream().allMatch(payment -> payment.getPaymentStatus() == PaymentStatus.PAID)) {
            loan.setStatus(LoanStatus.PAID);
            loan.setPaidOnDate(LocalDate.now());
        }
        int residualAmount = amount - scheduledPayment.getPaymentAmount();
        if (residualAmount != 0) {
            loan.setResidualAmount(loan.getResidualAmount() + residualAmount);
        }
        return loan;
    }

    @Override
    public List<Loan> getUserLoans(String username) {
        return loanRepository.findLoansByUsername(username);
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
