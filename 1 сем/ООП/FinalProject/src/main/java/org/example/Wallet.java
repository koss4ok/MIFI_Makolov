package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

public class Wallet {
    private List<Transaction> operations = new ArrayList<>();
    private Map<String, CategoryBudget> categories = new LinkedHashMap<>();

    public Wallet() {
        categories.put("GENERAL", new CategoryBudget("GENERAL", 0.0));
    }

    public List<Transaction> getOperations() {
        return operations;
    }
    public Map<String, CategoryBudget> getCategories() {
        return categories;
    }

    public void addCategory(String name) {
        categories.putIfAbsent(name, new CategoryBudget(name, 0.0));
    }

    public void setBudget(String category, double amount) {
        addCategory(category);
        categories.get(category).setBudget(amount);
    }

    public void addTransaction(Transaction t) {
        operations.add(t);
    }
    @JsonIgnore
    public double getIncomeTotal() {
        return operations.stream().filter(o -> o.getType() == Transaction.Type.INCOME).mapToDouble(Transaction::getAmount).sum();
    }
    @JsonIgnore
    public double getExpenseTotal() {
        return operations.stream().filter(o -> o.getType() == Transaction.Type.EXPENSE).mapToDouble(Transaction::getAmount).sum();
    }
    @JsonIgnore
    public double getBalance() {
        return Math.round((getIncomeTotal()-getExpenseTotal())*100.0)/100.0;
    }
    public double getIncomeByCategory(String cat) {
        return operations.stream()
                .filter(o -> o.getType() == Transaction.Type.INCOME && o.getCategory().equals(cat))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getExpenseByCategory(String cat) {
        return operations.stream().filter(o -> o.getType() == Transaction.Type.EXPENSE && o.getCategory().equals(cat))
                .mapToDouble(Transaction::getAmount).sum();
    }

    public String summaryLine() {
        return String.format("Доходы=%.2f, Расходы=%.2f, Баланс=%.2f", getIncomeTotal(), getExpenseTotal(), getBalance());
    }
}
