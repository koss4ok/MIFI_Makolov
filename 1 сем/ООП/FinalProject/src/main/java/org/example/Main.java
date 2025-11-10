package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI(new Scanner(System.in));
        UserStore userStore = new UserStore("data/users.json");
        AuthService auth = new AuthService(userStore);
        PersistenceService persistence = new PersistenceService("data");
        WalletService walletService = new WalletService();
        ReportService reportService = new ReportService();

        ui.println("Введите 'help' для списка команд.");

        String currentUser = null;
        Wallet currentWallet = null;

        while (true) {
            ui.print("> ");
            String line = ui.nextLine();
            if (line == null)
                break;
            String[] parts = CommandParser.split(line);
            if (parts.length == 0)
                continue;
            String cmd = parts[0].toLowerCase();

            try {
                switch (cmd) {
                    case "help" -> {
                        var helpText =
                        """
                        Команды:
                        register <login>                    — регистрация
                        login <login>                       — войти в УЗ
                        logout                              — выйти из УЗ
                        whoami                              — показать текущего пользователя
                        addcat <category>                   — добавить категорию
                        setbudget <category> <amount>       — установить бюджет категории
                        income <amount> [category] [note]   — добавить доход (категория по умолчанию GENERAL)
                        expense <amount> <category> [note]  — добавить расход
                        summary                             — общий итог по доходам/расходам и баланс
                        ops [limit]                         — последние операции
                        export <filename>                   — экспорт отчёта в файл
                        help                                — список команд
                        exit                                — сохранить и выйти""";
                        ui.println(helpText);
                    }
                    case "register" -> {
                        ui.ensureArgs(parts, 2, "Использование: register <login>");
                        String login = parts[1];
                        if (auth.userExists(login)) {
                            ui.println("Пользователь уже существует.");
                        } else {
                            String pwd = ui.readPassword("Пароль: ");
                            auth.register(login, pwd);
                            ui.println("Пользователь создан.");
                        }
                    }
                    case "login" -> {
                        ui.ensureArgs(parts, 2, "Использование: login <login>");
                        String login = parts[1];
                        String pwd = ui.readPassword("Пароль: ");
                        if (auth.login(login, pwd)) {
                            if (currentUser != null && currentWallet != null) {
                                persistence.saveWallet(currentUser, currentWallet);
                            }
                            currentUser = login;
                            currentWallet = persistence.loadWallet(login);
                            ui.println("Вход выполнен. Кошелёк загружен: " + currentWallet.summaryLine());
                        } else {
                            ui.println("Неверный логин или пароль.");
                        }
                    }
                    case "logout" -> {
                        if (currentUser == null) {
                            ui.println("Сначала войдите (login <login>).");
                        } else {
                            persistence.saveWallet(currentUser, currentWallet);
                            ui.println("Кошелёк сохранён. Выход из учётной записи " + currentUser + ".");
                            currentUser = null;
                            currentWallet = null;
                        }
                    }
                    case "whoami" -> {
                        ui.println(currentUser == null ? "(не авторизован)" : currentUser);
                    }
                    case "addcat" -> {
                        ensureAuth(currentUser);
                        ui.ensureArgs(parts, 2, "Использование: addcat <category>");
                        String cat = parts[1];
                        walletService.addCategory(currentWallet, cat);
                        ui.println("Категория добавлена: " + cat);
                    }
                    case "setbudget" -> {
                        ensureAuth(currentUser);
                        ui.ensureArgs(parts, 3, "Использование: setbudget <category> <amount>");
                        String cat = parts[1];
                        double amount = ui.parseAmount(parts[2]);
                        walletService.setBudget(currentWallet, cat, amount);
                        ui.println("Бюджет установлен: " + cat + " = " + amount);
                    }
                    case "income" -> {
                        ensureAuth(currentUser);
                        ui.ensureArgs(parts, 2, "Использование: income <amount> [category] [note...]");
                        double amount = ui.parseAmount(parts[1]);
                        String category = parts.length >= 3 ? parts[2] : "GENERAL";
                        String note = parts.length >= 4 ? CommandParser.joinFrom(parts, 3) : "";
                        walletService.addIncome(currentWallet, amount, category, note);
                        ui.println("Доход добавлен. Баланс: " + currentWallet.getBalance());
                    }
                    case "expense" -> {
                        ensureAuth(currentUser);
                        ui.ensureArgs(parts, 3, "Использование: expense <amount> <category> [note...]");
                        double amount = ui.parseAmount(parts[1]);
                        String category = parts[2];
                        String note = parts.length >= 4 ? CommandParser.joinFrom(parts, 3) : "";
                        var alert = walletService.addExpense(currentWallet, amount, category, note);
                        ui.println("Расход добавлен. Баланс: " + currentWallet.getBalance());
                        if (alert.budgetExceeded()) {
                            ui.println("⚠ Превышен лимит по категории '" + category + "'. Перерасход: " + alert.getOverBy());
                        }
                        if (alert.expensesExceedIncome()) {
                            ui.println("⚠ Расходы превысили доходы!");
                        }
                    }
                    case "summary" -> {
                        ensureAuth(currentUser);
                        ui.println(reportService.summary(currentWallet));
                    }
                    case "ops" -> {
                        ensureAuth(currentUser);
                        int limit = 20;
                        if (parts.length >= 2) {
                            limit = Integer.parseInt(parts[1]);
                        }
                        ui.println(reportService.lastOperations(currentWallet, limit));
                    }
                    case "export" -> {
                        ensureAuth(currentUser);
                        ui.ensureArgs(parts, 2, "Использование: export <filename>");
                        String fn = parts[1];
                        reportService.exportReport(currentWallet, fn);
                        ui.println("Отчёт сохранён: " + fn);
                    }
                    case "exit" -> {
                        if (currentUser != null && currentWallet != null) {
                            persistence.saveWallet(currentUser, currentWallet);
                            ui.println("Кошелёк сохранён для " + currentUser + ".");
                        }
                        ui.println("Пока!");
                        return;
                    }
                    default -> ui.println("Неизвестная команда. Введите 'help'.");
                }
            } catch (Exception e) {
                ui.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static void ensureAuth(String currentUser) throws Exception {
        if (currentUser == null)
            throw new Exception("Сначала войдите: login <login>");
    }
}