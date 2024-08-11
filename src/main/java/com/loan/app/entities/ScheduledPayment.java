package com.loan.app.entities;

import com.loan.app.enums.PaymentStatus;

import java.io.Serializable;
import java.time.LocalDate;

public class ScheduledPayment implements Serializable {
    private int loanId;
    private final int paymentAmount;
    private final LocalDate paymentDueDate;
    private PaymentStatus paymentStatus;
    private LocalDate paidOnDate;

    public ScheduledPayment(int paymentAmount, LocalDate paymentDueDate, PaymentStatus paymentStatus) {
        this.paymentAmount = paymentAmount;
        this.paymentDueDate = paymentDueDate;
        this.paymentStatus = paymentStatus;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setPaidOnDate(LocalDate paidOnDate) {
        this.paidOnDate = paidOnDate;
    }

    public int getLoanId() {
        return loanId;
    }

    public int getPaymentAmount() {
        return paymentAmount;
    }

    public LocalDate getPaymentDueDate() {
        return paymentDueDate;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDate getPaidOnDate() {
        return paidOnDate;
    }
}
