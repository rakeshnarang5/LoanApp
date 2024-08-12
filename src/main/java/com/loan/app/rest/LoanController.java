package com.loan.app.rest;

import com.loan.app.dtos.LoanRequestDTO;
import com.loan.app.dtos.LoanResponseDTO;
import com.loan.app.entities.Loan;
import com.loan.app.services.LoanService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public LoanResponseDTO applyForLoan(@RequestBody final LoanRequestDTO loanRequest, Authentication authentication) {
        return loanService.createLoan(loanRequest, authentication.getName());
    }

    @GetMapping("/view")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public List<Loan> getLoans(Authentication authentication) {
        return loanService.getUserLoans(authentication.getName());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<Loan> getPendingLoans() {
        return loanService.getPendingLoans();
    }

    @GetMapping("/viewAll")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }

    @PatchMapping("/approve/{loanId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Loan approveLoan(@PathVariable("loanId") final int loanId) {
        return loanService.approveLoan(loanId);
    }

    @PatchMapping("repay/{loanId}/{amount}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public Loan repayLoan(@PathVariable("loanId") final int loanId, @PathVariable("amount") final int amount, Authentication authentication) {
        return loanService.repay(loanId, amount, authentication.getName());
    }

}
