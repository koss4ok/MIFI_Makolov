package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WalletServiceTest {

    @Test
    void addIncomeExpenseBudgetAlertsWork() throws Exception {
        Wallet w = new Wallet();
        WalletService ws = new WalletService();

        ws.addCategory(w,"FOOD");
        ws.setBudget(w,"FOOD",100);

        ws.addIncome(w,250,"GENERAL","salary");

        WalletService.BudgetAlert a1 = ws.addExpense(w,30,"FOOD","milk");
        assertFalse(a1.budgetExceeded());
        assertFalse(a1.expensesExceedIncome());

        WalletService.BudgetAlert a2 = ws.addExpense(w,80,"FOOD","dinner");
        assertTrue(a2.budgetExceeded());
        assertEquals(10.00,a2.getOverBy(),0.01);
        assertFalse(a2.expensesExceedIncome());
    }

    @Test
    void missingCategoryExpenseFails() {
        Wallet w = new Wallet();
        WalletService ws = new WalletService();

        assertThrows(Exception.class, () -> ws.addExpense(w,10,"NOPE",""));
    }

    @Test
    void addCategoryValidation() {
        Wallet w = new Wallet();
        WalletService ws = new WalletService();

        assertThrows(Exception.class, () -> ws.addCategory(w," "));
    }
}
