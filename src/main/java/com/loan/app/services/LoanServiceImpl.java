package com.loan.app.services;

import com.loan.app.dtos.LoanRequestDTO;
import com.loan.app.dtos.LoanResponseDTO;
import com.loan.app.entities.Loan;
import com.loan.app.entities.ScheduledPayment;
import com.loan.app.enums.LoanStatus;
import com.loan.app.enums.PaymentStatus;
import com.loan.app.exceptions.LoanException;
import com.loan.app.repositories.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanServiceImpl implements LoanService {

    private final Logger logger = LoggerFactory.getLogger(LoanServiceImpl.class);

    private final LoanRepository loanRepository;

    public LoanServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Value("${loan.app.minimum.threshold}")
    private int minimumLoanValue;

    @Override
    public LoanResponseDTO createLoan(LoanRequestDTO loanRequest, String username) {
        logger.info("Started creating loan for user: {} with amount: {} and tenure: {}", username, loanRequest.amount(), loanRequest.term());
        validateLoanRequest(loanRequest);
        Loan loan = getLoan(loanRequest, username);
        loanRepository.save(loan);
        logger.info("Loan created successfully in pending state with id: {}", loan.getLoanId());
        return new LoanResponseDTO(loanRequest, loan);
    }

    private static Loan getLoan(LoanRequestDTO loanRequest, String username) {
        Loan loan = new Loan(loanRequest.amount(), loanRequest.term(), username, LoanStatus.PENDING);
        int equalWeeklyInstallment = loanRequest.amount() / loanRequest.term();
        int roundingError = loanRequest.amount() - (equalWeeklyInstallment * loanRequest.term());
        LocalDate currentDate = LocalDate.now();
        for (int termIterator = 0; termIterator < loanRequest.term(); termIterator++) {
            LocalDate installmentDate = currentDate.plusWeeks(1);
            if (termIterator == loanRequest.term() - 1) {
                loan.addScheduledPayment(new ScheduledPayment(equalWeeklyInstallment + roundingError, installmentDate, PaymentStatus.PENDING));
                continue;
            }
            loan.addScheduledPayment(new ScheduledPayment(equalWeeklyInstallment, installmentDate, PaymentStatus.PENDING));
            currentDate = installmentDate;
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
        validateLoan(loanId, username, loan);
        logger.info("Loan with id: {} validated successfully", loanId);
        ScheduledPayment scheduledPayment = loan.getScheduledPayments().stream()
                .filter(payment -> payment.getPaymentStatus() == PaymentStatus.PENDING)
                .findFirst().orElse(null);
        validateScheduledPayment(loanId, amount, scheduledPayment, loan);
        updateScheduledPayment(scheduledPayment);
        if (loan.getScheduledPayments().stream().allMatch(payment -> payment.getPaymentStatus() == PaymentStatus.PAID)) {
            updateLoan(loan);
        }
        updateResidualAmount(amount, scheduledPayment, loan);
        logger.info("User: {} made successful payment towards their loan: {} with amount: {} and dated: {}", username, loanId, scheduledPayment.getPaymentAmount(), scheduledPayment.getPaymentDueDate());
        return loan;
    }

    private static void updateResidualAmount(int amount, ScheduledPayment scheduledPayment, Loan loan) {
        int residualAmount = amount - scheduledPayment.getPaymentAmount();
        if (residualAmount != 0) {
            loan.setResidualAmount(loan.getResidualAmount() + residualAmount);
        }
    }

    private static void updateLoan(Loan loan) {
        loan.setStatus(LoanStatus.PAID);
        loan.setPaidOnDate(LocalDate.now());
    }

    private static void updateScheduledPayment(ScheduledPayment scheduledPayment) {
        scheduledPayment.setPaymentStatus(PaymentStatus.PAID);
        scheduledPayment.setPaidOnDate(LocalDate.now());
    }

    private static void validateScheduledPayment(int loanId, int amount, ScheduledPayment scheduledPayment, Loan loan) {
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
    }

    private static void validateLoan(int loanId, String username, Loan loan) {
        if (loan == null) {
            throw new LoanException("Loan with this id doesn't exist: " + loanId);
        }
        if (!loan.getUser().equals(username)){
            throw new LoanException("Loan belongs to different user: "+ loan.getUser());
        }
        if (loan.getStatus() == LoanStatus.PENDING) {
            throw new LoanException("Loan is in pending state");
        }
        if (loan.getStatus() == LoanStatus.PAID) {
            throw new LoanException("Loan is already paid off.");
        }
    }

    @Override
    public List<Loan> getUserLoans(String username) {
        return loanRepository.findLoansByUsername(username);
    }

    private void validateLoanRequest(LoanRequestDTO loanRequest) {
        if (loanRequest.amount() < minimumLoanValue) {
            throw new LoanException("Minimum Loan Amount is $"+minimumLoanValue);
        }
        if (loanRequest.term() < 1) {
            throw new LoanException("Minimum Loan Term is 1w");
        }
    }

}
