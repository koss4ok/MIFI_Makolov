package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WalletCoreTest {

    @Test
    void totalsComputeCorrectly() throws Exception {
        Wallet w = new Wallet();
        WalletService ws = new WalletService();

        ws.addIncome(w,1000,"GENERAL","salary");
        w.addCategory("FOOD");
        ws.addExpense(w,200,"FOOD","eat");

        assertEquals(1000.0, w.getIncomeTotal(),0.001);
        assertEquals(200.0, w.getExpenseTotal(),0.001);
        assertEquals(800.0, w.getBalance(),0.001);
    }
}
