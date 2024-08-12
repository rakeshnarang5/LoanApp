package com.loan.app.services;

import com.loan.app.dtos.LoanRequestDTO;
import com.loan.app.dtos.LoanResponseDTO;
import com.loan.app.entities.Loan;
import com.loan.app.entities.ScheduledPayment;
import com.loan.app.enums.LoanStatus;
import com.loan.app.enums.PaymentStatus;
import com.loan.app.exceptions.LoanException;
import com.loan.app.repositories.LoanRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class LoanServiceImplTest {

    @InjectMocks
    private LoanServiceImpl loanServiceImpl;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanRequestDTO request;

    @Mock
    private Loan loan;

    @Mock
    private ScheduledPayment scheduledPayment;

    @Test
    public void testRepayValidRequest() {
        ScheduledPayment scheduledPayment = new ScheduledPayment(10000, LocalDate.now().plusWeeks(1), PaymentStatus.PENDING);
        Loan loan = new Loan(10000,1,"rohan", LoanStatus.APPROVED);
        loan.addScheduledPayment(scheduledPayment);
        Mockito.when(loanRepository.findById(1)).thenReturn(loan);
        Loan repaidLoan = loanServiceImpl.repay(1, 10500, "rohan");
        Assertions.assertEquals(PaymentStatus.PAID, repaidLoan.getScheduledPayments().get(0).getPaymentStatus());
        Assertions.assertEquals(LocalDate.now(), repaidLoan.getScheduledPayments().get(0).getPaidOnDate());
        Assertions.assertEquals(LoanStatus.PAID, repaidLoan.getStatus());
        Assertions.assertEquals(LocalDate.now(), repaidLoan.getPaidOnDate());
        Assertions.assertEquals(500, repaidLoan.getResidualAmount());
    }

    @Test
    public void testRepayInvalidRequestDueDatePassed(){
        LoanException loanException = null;
        Mockito.when(scheduledPayment.getPaymentStatus()).thenReturn(PaymentStatus.PENDING);
        Mockito.when(scheduledPayment.getPaymentDueDate()).thenReturn(LocalDate.now().minusWeeks(1));
        Mockito.when(loan.getScheduledPayments()).thenReturn(List.of(scheduledPayment));
        Mockito.when(loan.getUser()).thenReturn("rohan");
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.APPROVED);
        Mockito.when(loanRepository.findById(1)).thenReturn(loan);
        try{
            loanServiceImpl.repay(1,2500, "rohan");
        } catch (LoanException e){
            loanException=e;
        }
        Assertions.assertNotNull(loanException);
        Assertions.assertEquals("Payment cannot be done after due date, loan is moved Defaulted state", loanException.getMessage());
    }

    @Test
    public void testRepayInvalidRequestAmountPaidLess(){
        LoanException loanException = null;
        Mockito.when(scheduledPayment.getPaymentAmount()).thenReturn(3333);
        Mockito.when(scheduledPayment.getPaymentStatus()).thenReturn(PaymentStatus.PENDING);
        Mockito.when(scheduledPayment.getPaymentDueDate()).thenReturn(LocalDate.now().plusWeeks(1));
        Mockito.when(loan.getScheduledPayments()).thenReturn(List.of(scheduledPayment));
        Mockito.when(loan.getUser()).thenReturn("rohan");
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.APPROVED);
        Mockito.when(loanRepository.findById(1)).thenReturn(loan);
        try{
            loanServiceImpl.repay(1,2500, "rohan");
        } catch (LoanException e){
            loanException=e;
        }
        Assertions.assertNotNull(loanException);
        Assertions.assertEquals("Minimum payment amount is: 3333", loanException.getMessage());
    }

    @Test
    public void testRepayInvalidRequestLoanCompletedState(){
        LoanException loanException = null;
        Mockito.when(loan.getUser()).thenReturn("rohan");
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.PAID);
        Mockito.when(loanRepository.findById(1)).thenReturn(loan);
        try{
            loanServiceImpl.repay(1,3400, "rohan");
        } catch (LoanException e){
            loanException=e;
        }
        Assertions.assertNotNull(loanException);
        Assertions.assertEquals("Loan is already paid off.", loanException.getMessage());
    }

    @Test
    public void testRepayInvalidRequestLoanInPendingState(){
        LoanException loanException = null;
        Mockito.when(loan.getUser()).thenReturn("rohan");
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.PENDING);
        Mockito.when(loanRepository.findById(1)).thenReturn(loan);
        try{
            loanServiceImpl.repay(1,3400, "rohan");
        } catch (LoanException e){
            loanException=e;
        }
        Assertions.assertNotNull(loanException);
        Assertions.assertEquals("Loan is in pending state", loanException.getMessage());
    }

    @Test
    public void testRepayInvalidRequestLoanBelongsToDifferentUser(){
        LoanException loanException = null;
        Mockito.when(loan.getUser()).thenReturn("rakesh");
        Mockito.when(loanRepository.findById(1)).thenReturn(loan);
        try{
            loanServiceImpl.repay(1,3400, "rohan");
        } catch (LoanException e){
            loanException=e;
        }
        Assertions.assertNotNull(loanException);
        Assertions.assertEquals("Loan belongs to different user: rakesh", loanException.getMessage());
    }

    @Test
    public void testRepayInvalidRequestLoanDoesNotExist(){
        LoanException loanException = null;
        try{
            loanServiceImpl.repay(1,3400, "rohan");
        } catch (LoanException e){
            loanException=e;
        }
        Assertions.assertNotNull(loanException);
        Assertions.assertEquals("Loan with this id doesn't exist: 1", loanException.getMessage());
    }

    @Test
    public void testCreateLoanValidLoan(){
        Mockito.when(request.amount()).thenReturn(10000);
        Mockito.when(request.term()).thenReturn(3);
        LoanResponseDTO response = loanServiceImpl.createLoan(request, "rohan");
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.loanRequest());
        Assertions.assertEquals(request, response.loanRequest());
        Loan loan = response.loan();
        Assertions.assertNotNull(loan);
        Assertions.assertEquals(LoanStatus.PENDING, loan.getStatus());
        Assertions.assertEquals(10000, loan.getAmount());
        Assertions.assertEquals(3, loan.getTenure());
        Assertions.assertEquals(0, loan.getLoanId());
        Assertions.assertEquals("rohan", loan.getUser());
        Assertions.assertEquals(3, loan.getScheduledPayments().size());
        LocalDate localDate = LocalDate.now();
        ScheduledPayment scheduledPayment1 = loan.getScheduledPayments().get(0);
        Assertions.assertEquals(3333, scheduledPayment1.getPaymentAmount());
        Assertions.assertEquals(localDate.plusWeeks(1L), scheduledPayment1.getPaymentDueDate());
        ScheduledPayment scheduledPayment2 = loan.getScheduledPayments().get(1);
        Assertions.assertEquals(3333, scheduledPayment2.getPaymentAmount());
        Assertions.assertEquals(localDate.plusWeeks(2L), scheduledPayment2.getPaymentDueDate());
        ScheduledPayment scheduledPayment3 = loan.getScheduledPayments().get(2);
        Assertions.assertEquals(3334, scheduledPayment3.getPaymentAmount());
        Assertions.assertEquals(localDate.plusWeeks(3L), scheduledPayment3.getPaymentDueDate());
    }

    @Test
    public void testCreateLoanInvalidLoanRequestTerm() {
        Mockito.when(request.amount()).thenReturn(500);
        Mockito.when(request.term()).thenReturn(0);
        LoanException loanException = null;
        try{
            loanServiceImpl.createLoan(request, "rohan");
        } catch (LoanException ex){
            loanException=ex;
        }
        Assertions.assertNotNull(loanException);
        Assertions.assertEquals("Minimum Loan Term is 1w", loanException.getMessage());
    }

    @Test
    public void testCreateLoanInvalidLoanRequestAmount() {
        Mockito.when(request.amount()).thenReturn(50);
        LoanException loanException = null;
        try{
            loanServiceImpl.createLoan(request, "rohan");
        } catch (LoanException ex){
            loanException=ex;
        }
        Assertions.assertNotNull(loanException);
        Assertions.assertEquals("Minimum Loan Amount is $100", loanException.getMessage());
    }

}
