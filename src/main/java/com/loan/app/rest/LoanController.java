package com.loan.app.rest;

import com.loan.app.dtos.LoanRequestDTO;
import com.loan.app.dtos.LoanResponseDTO;
import com.loan.app.entities.Loan;
import com.loan.app.services.LoanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public LoanResponseDTO applyForLoan(@RequestBody final LoanRequestDTO loanRequest) {
        return loanService.createLoan(loanRequest);
    }

    @GetMapping("/pending")
    public List<Loan> getPendingLoans(){
        return loanService.getPendingLoans();
    }

    @GetMapping
    public List<Loan> getAllLoans(){
        return loanService.getAllLoans();
    }

    @PatchMapping("/{loanId}/approve")
    public Loan approveLoan(@PathVariable("loanId") final int loanId) {
        return loanService.approveLoan(loanId);
    }

    @PatchMapping("/{loanId}/repay/{amount}")
    public Loan repayLoan(@PathVariable("loanId") final int loanId,
                          @PathVariable("amount") final int amount){
        return loanService.repay(loanId,amount);
    }

}
