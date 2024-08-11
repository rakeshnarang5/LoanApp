package com.loan.app.repositories;

import com.loan.app.entities.Loan;
import com.loan.app.entities.User;
import com.loan.app.enums.LoanStatus;
import com.loan.app.enums.UserType;
import com.loan.app.exceptions.LoanException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class LoanRepository {

    private static final List<Loan> LOANS = new ArrayList<>();

    private static final AtomicInteger loanIdGenerator = new AtomicInteger();

    public List<Loan> findAll() {
        return new ArrayList<>(LOANS);
    }

    public Loan findById(int loanId) {
        return LOANS.stream()
                .filter(loan -> loan.getLoanId() == loanId)
                .findFirst().orElse(null);
    }

    public List<Loan> findLoansByUser(User user){
        return LOANS.stream().filter(loan -> loan.getLoaner() == user).toList();
    }

    public List<Loan> findPendingLoans(User user) {
        if (user.getType() == UserType.ADMIN){
            return LOANS.stream().filter(loan -> loan.getStatus() == LoanStatus.PENDING).toList();
        }
        return new ArrayList<>();
    }

    public void save(Loan loan) {
        int loanId = loanIdGenerator.incrementAndGet();
        loan.setLoanId(loanId);
        loan.getScheduledPayments().forEach(scheduledPayment -> {
            scheduledPayment.setLoanId(loanId);
        });
        LOANS.add(loan);
    }

    public Loan approveLoan(int loanId) {
        Loan loan = findById(loanId);
        if (loan == null) {
            throw new LoanException("Loan does not exist: " + loanId);
        }
        loan.setStatus(LoanStatus.APPROVED);
        return loan;
    }
}
