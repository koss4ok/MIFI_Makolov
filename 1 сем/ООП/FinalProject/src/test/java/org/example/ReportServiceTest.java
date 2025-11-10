package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ReportServiceTest {

    @TempDir
    Path tmp;

    private Wallet makeSample() throws Exception {
        Wallet w = new Wallet();
        WalletService ws = new WalletService();

        ws.addCategory(w,"FOOD");
        ws.addCategory(w,"FUN");
        ws.setBudget(w,"FOOD",150);

        ws.addIncome(w,500,"GENERAL","salary");
        ws.addExpense(w,120,"FOOD","groceries");
        ws.addExpense(w,50,"FUN","movies");

        return w;
    }

    @Test
    void summaryContainsBalanceAndCategories() throws Exception {
        ReportService r = new ReportService();
        String s = r.summary(makeSample());

        assertTrue(s.contains("Баланс"));
        assertTrue(s.contains("FOOD"));
        assertTrue(s.contains("FUN"));
    }

    @Test
    void lastOperationsShowsSigns() throws Exception {
        ReportService r = new ReportService();
        Wallet w = makeSample();

        String s = r.lastOperations(w,1);
        assertTrue(s.contains("+") || s.contains("-"));
    }

    @Test
    void exportReportWritesFileSuccessfully() throws Exception {
        ReportService r = new ReportService();
        Wallet w = makeSample();

        Path f = tmp.resolve("rep.txt");
        r.exportReport(w,f.toString());

        assertTrue(Files.exists(f));
        assertTrue(Files.readString(f).contains("Сводка"));
    }
}
