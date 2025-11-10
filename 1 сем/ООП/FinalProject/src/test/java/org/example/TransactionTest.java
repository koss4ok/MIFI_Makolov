package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {

    @Test
    void incomeFactoryCreatesCorrectTransaction() {
        Transaction t = Transaction.income(10,"GENERAL","note");
        assertEquals(Transaction.Type.INCOME,t.getType());
        assertEquals(10.0,t.getAmount());
        assertEquals("GENERAL",t.getCategory());
        assertNotNull(t.getTimestamp());
    }

    @Test
    void expenseFactoryCreatesCorrectTransaction() {
        Transaction t = Transaction.expense(5,"FOOD","pizza");
        assertEquals(Transaction.Type.EXPENSE,t.getType());
        assertEquals(5.0,t.getAmount());
        assertEquals("FOOD",t.getCategory());
        assertNotNull(t.getTimestamp());
    }
}
