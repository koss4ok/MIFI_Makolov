package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenceServiceTest {

    @TempDir
    Path tmp;

    @Test
    void loadCreatesNewWalletIfMissing() {
        PersistenceService p = new PersistenceService(tmp.toString());
        Wallet w = p.loadWallet("alice");

        assertNotNull(w);
        assertEquals(0.0, w.getIncomeTotal(),0.01);
    }

    @Test
    void saveThenLoadKeepsData() throws Exception {
        PersistenceService p = new PersistenceService(tmp.toString());

        Wallet w = new Wallet();
        WalletService ws = new WalletService();
        ws.addCategory(w,"FOOD");
        ws.setBudget(w,"FOOD",100);
        ws.addIncome(w,300,"GENERAL","salary");
        ws.addExpense(w,50,"FOOD","milk");

        p.saveWallet("bob",w);
        assertTrue(Files.exists(tmp.resolve("bob-wallet.json")));

        Wallet w2 = p.loadWallet("bob");
        assertEquals(300.0,w2.getIncomeTotal(),0.01);
        assertEquals(50.0,w2.getExpenseTotal(),0.01);
        assertEquals(100.0,w2.getCategories().get("FOOD").getBudget(),0.01);
    }
}
