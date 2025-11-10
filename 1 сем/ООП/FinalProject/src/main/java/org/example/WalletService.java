package org.example;

public class WalletService {

    public void addCategory(Wallet w, String category) throws Exception {
        if (category == null || category.isBlank())
            throw new Exception("Категория не может быть пустой.");
        w.addCategory(category);
    }

    public void setBudget(Wallet w, String category, double amount) throws Exception {
        if (category == null || category.isBlank())
            throw new Exception("Категория не может быть пустой.");
        w.setBudget(category, amount);
    }

    public void addIncome(Wallet w, double amount, String category, String note) {
        w.addCategory(category);
        w.addTransaction(Transaction.income(amount, category, note));
    }

    public BudgetAlert addExpense(Wallet w, double amount, String category, String note) throws Exception {
        if (!w.getCategories().containsKey(category))
            throw new Exception("Категория не найдена: " + category);
        w.addTransaction(Transaction.expense(amount, category, note));

        double spent = w.getExpenseByCategory(category);
        double budget = w.getCategories().get(category).getBudget();
        boolean over = budget > 0 && spent > budget;
        boolean neg = w.getExpenseTotal() > w.getIncomeTotal();
        double overBy = over ? Math.round((spent - budget) * 100.0) / 100.0 : 0.0;
        return new BudgetAlert(over, overBy, neg);
    }

    public static class BudgetAlert {
        private final boolean budgetExceeded;
        private final double overBy;
        private final boolean expensesExceedIncome;

        public BudgetAlert(boolean budgetExceeded, double overBy, boolean expensesExceedIncome) {
            this.budgetExceeded = budgetExceeded;
            this.overBy = overBy;
            this.expensesExceedIncome = expensesExceedIncome;
        }
        public boolean budgetExceeded() {
            return budgetExceeded;
        }
        public double getOverBy() {
            return overBy;
        }
        public boolean expensesExceedIncome() {
            return expensesExceedIncome;
        }
    }
}
