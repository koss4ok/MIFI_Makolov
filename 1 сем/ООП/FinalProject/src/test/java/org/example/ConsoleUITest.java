package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Scanner;

public class ConsoleUITest {

    @Test
    void parseAmountRoundsCorrectly() throws Exception {
        ConsoleUI ui = new ConsoleUI(new Scanner(""));
        assertEquals(12.34, ui.parseAmount("12.343"), 0.0);
    }

    @Test
    void parseAmountRejectsNegative() {
        ConsoleUI ui = new ConsoleUI(new Scanner(""));
        assertThrows(Exception.class, () -> ui.parseAmount("-1"));
    }

    @Test
    void ensureArgsThrowsCorrectly() {
        ConsoleUI ui = new ConsoleUI(new Scanner(""));
        var ex = assertThrows(Exception.class,
                () -> ui.ensureArgs(new String[]{"one"},2,"usage")
        );
        assertEquals("usage",ex.getMessage());
    }
}
