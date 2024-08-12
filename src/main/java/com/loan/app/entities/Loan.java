package com.loan.app.entities;

import com.loan.app.enums.LoanStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Loan implements Serializable {
    private int loanId;
    private final int amount;
    private final int tenure;
    private final String loaner;
    private LoanStatus status;
    private final List<ScheduledPayment> scheduledPayments = new ArrayList<>();
    private int residualAmount;
    private LocalDate paidOnDate;

    public Loan(int amount, int tenure, String loaner, LoanStatus status) {
        this.amount = amount;
        this.tenure = tenure;
        this.loaner = loaner;
        this.status = status;
    }

    public LocalDate getPaidOnDate() {
        return paidOnDate;
    }

    public void setPaidOnDate(LocalDate paidOnDate) {
        this.paidOnDate = paidOnDate;
    }

    public int getResidualAmount() {
        return residualAmount;
    }

    public void setResidualAmount(int residualAmount) {
        this.residualAmount = residualAmount;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public void addScheduledPayment(ScheduledPayment scheduledPayment){
        this.scheduledPayments.add(scheduledPayment);
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public int getLoanId() {
        return loanId;
    }

    public int getAmount() {
        return amount;
    }

    public int getTenure() {
        return tenure;
    }

    public String getLoaner() {
        return loaner;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public List<ScheduledPayment> getScheduledPayments() {
        return new ArrayList<>(scheduledPayments);
    }
}
