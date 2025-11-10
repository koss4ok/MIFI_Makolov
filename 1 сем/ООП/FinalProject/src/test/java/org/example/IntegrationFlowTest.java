package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationFlowTest {

    @TempDir
    Path tmp;

    @Test
    void fullFlowRegisterLoginSaveReload() throws Exception {

        UserStore store = new UserStore(tmp.resolve("users.json").toString());
        AuthService auth = new AuthService(store);
        PersistenceService ps = new PersistenceService(tmp.toString());
        WalletService ws = new WalletService();

        // register + login
        auth.register("ev","pass1234");
        assertTrue(auth.login("ev","pass1234"));

        Wallet w = ps.loadWallet("ev");

        ws.addCategory(w,"FOOD");
        ws.setBudget(w,"FOOD",200);
        ws.addIncome(w,1000,"GENERAL","salary");

        var alert = ws.addExpense(w,250,"FOOD","week");
        assertTrue(alert.budgetExceeded());
        assertFalse(alert.expensesExceedIncome());

        ps.saveWallet("ev",w);

        Wallet w2 = ps.loadWallet("ev");

        assertEquals(1000.0,w2.getIncomeTotal(),0.01);
        assertEquals(250.0,w2.getExpenseTotal(),0.01);
        assertEquals(200.0,w2.getCategories().get("FOOD").getBudget(),0.01);
    }
}
