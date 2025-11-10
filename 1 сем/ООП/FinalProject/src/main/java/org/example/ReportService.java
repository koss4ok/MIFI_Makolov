package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportService {
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String summary(Wallet w) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Сводка ===\n");
        sb.append(String.format("Доходы: %.2f\n", w.getIncomeTotal()));
        sb.append(String.format("Расходы: %.2f\n", w.getExpenseTotal()));
        sb.append(String.format("Баланс:  %.2f\n", w.getBalance()));
        sb.append("\nКатегории (доходы/расходы/чистое) и бюджеты:\n");
        for (var e : w.getCategories().values()) {
            String c = e.getName();
            double inc = w.getIncomeByCategory(c);
            double exp = w.getExpenseByCategory(c);
            double net = inc - exp;
            double budget = e.getBudget();
            double left = budget - exp; // бюджет касается расходов
            sb.append(String.format(
                    " - %s: доходы=%.2f, расходы=%.2f, чистое=%.2f | бюджет=%.2f, остаток=%.2f\n",
                    c, inc, exp, net, budget, left
            ));
        }
        return sb.toString();
    }


    public String lastOperations(Wallet w, int limit) {
        var ops = w.getOperations();
        int from = Math.max(0, ops.size() - limit);
        StringBuilder sb = new StringBuilder("=== Последние операции ===\n");
        for (int i = ops.size()-1; i >= from; i--) {
            var t = ops.get(i);
            sb.append(String.format("%s  %s  %-7s  %-12s  %.2f  %s\n",
                    DF.format(t.getTimestamp()),
                    t.getType()== Transaction.Type.INCOME?"+":"-",
                    t.getType(), t.getCategory(), t.getAmount(), Optional.ofNullable(t.getNote()).orElse("")));
        }
        return sb.toString();
    }

    public void exportReport(Wallet w, String filename) throws IOException {
        String content = summary(w) + "\n" + lastOperations(w, 1000);
        Files.writeString(Path.of(filename), content);
    }
}
