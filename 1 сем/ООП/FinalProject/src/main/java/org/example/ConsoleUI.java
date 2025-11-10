package org.example;

import java.io.Console;
import java.util.Locale;
import java.util.Scanner;

public class ConsoleUI {
    private final Scanner scanner;

    public ConsoleUI(Scanner scanner) {
        this.scanner = scanner;
        Locale.setDefault(Locale.US);
    }

    public void println(String s) { System.out.println(s); }
    public void print(String s) { System.out.print(s); }

    public String nextLine() {
        try {
            return scanner.nextLine().trim();
        } catch (Exception e) {
            return null;
        }
    }

    public void ensureArgs(String[] parts, int min, String usage) throws Exception {
        if (parts.length < min)
            throw new Exception(usage);
    }

    public double parseAmount(String s) throws Exception {
        try {
            double v = Double.parseDouble(s);
            if (Double.isNaN(v) || Double.isInfinite(v) || v < 0) throw new NumberFormatException();
            return Math.round(v * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            throw new Exception("Сумма должна быть неотрицательным числом (до 2 знаков).");
        }
    }

    public String readPassword(String prompt) {
        Console c = System.console();
        if (c != null) {
            char[] ch = c.readPassword(prompt);
            return new String(ch);
        } else {
            print(prompt);
            return scanner.nextLine();
        }
    }
}
