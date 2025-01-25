package expensetracker;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

// Expense Class
class Expense {
    private int id;
    private LocalDate date;
    private double amount;
    private String category;
    private String description;

    public Expense(int id, LocalDate date, double amount, String category, String description) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Date: " + date + ", Amount: " + amount + 
               ", Category: " + category + ", Description: " + description;
    }

    public String toFileFormat() {
        return id + "," + date + "," + amount + "," + category + "," + description;
    }

    public static Expense fromFileFormat(String line) {
        String[] parts = line.split(",");
        return new Expense(
                Integer.parseInt(parts[0]),
                LocalDate.parse(parts[1]),
                Double.parseDouble(parts[2]),
                parts[3],
                parts[4]
        );
    }
}

// FileManager Class for File Operations
class FileManager {
    private final String filePath;

    public FileManager(String filePath) {
        this.filePath = filePath;
    }

    public List<Expense> loadExpenses() {
        List<Expense> expenses = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                expenses.add(Expense.fromFileFormat(line));
            }
        } catch (FileNotFoundException e) {
            System.out.println("No previous expense records found. Starting fresh.");
        } catch (IOException e) {
            System.out.println("Error reading expenses: " + e.getMessage());
        }
        return expenses;
    }

    public void saveExpenses(List<Expense> expenses) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Expense expense : expenses) {
                writer.write(expense.toFileFormat());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving expenses: " + e.getMessage());
        }
    }
}

// ExpenseManager Class
class ExpenseManager {
    private List<Expense> expenses;
    private int expenseCounter;
    private final FileManager fileManager;

    public ExpenseManager(String filePath) {
        this.fileManager = new FileManager(filePath);
        this.expenses = fileManager.loadExpenses();
        this.expenseCounter = expenses.stream()
                .mapToInt(Expense::getId)
                .max()
                .orElse(0) + 1;
    }

    public void addExpense(LocalDate date, double amount, String category, String description) {
        Expense expense = new Expense(expenseCounter++, date, amount, category, description);
        expenses.add(expense);
        fileManager.saveExpenses(expenses); // Save automatically
        System.out.println("Expense added and saved successfully.");
    }

    public void viewSummary(String period, LocalDate startDate, LocalDate endDate) {
        List<Expense> filteredExpenses = expenses.stream()
                .filter(e -> !e.getDate().isBefore(startDate) && !e.getDate().isAfter(endDate))
                .toList();

        if (filteredExpenses.isEmpty()) {
            System.out.println("No expenses found for the " + period + ".");
        } else {
            filteredExpenses.forEach(System.out::println);
            double total = filteredExpenses.stream().mapToDouble(Expense::getAmount).sum();
            System.out.println("Total Expenses for the " + period + ": " + total);
        }
    }
}

// Main Application
public class ExpenseTracker {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ExpenseManager expenseManager = new ExpenseManager("expenses.txt");

    public static void main(String[] args) {
        System.out.println("Welcome to the Daily Expense Tracker!");
        while (true) {
            displayMenu();
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1 -> addExpense();
                case 2 -> viewDailySummary();
                case 3 -> viewWeeklySummary();
                case 4 -> viewMonthlySummary();
                case 5 -> exitApplication();
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void displayMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Add Expense");
        System.out.println("2. View Daily Summary");
        System.out.println("3. View Weekly Summary");
        System.out.println("4. View Monthly Summary");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void addExpense() {
        System.out.print("Enter date (yyyy-mm-dd): ");
        LocalDate date = LocalDate.parse(scanner.nextLine());
        System.out.print("Enter amount: ");
        double amount = Double.parseDouble(scanner.nextLine());
        System.out.print("Enter category: ");
        String category = scanner.nextLine();
        System.out.print("Enter description: ");
        String description = scanner.nextLine();
        expenseManager.addExpense(date, amount, category, description);
    }

    private static void viewDailySummary() {
        System.out.print("Enter date (yyyy-mm-dd): ");
        LocalDate date = LocalDate.parse(scanner.nextLine());
        expenseManager.viewSummary("day", date, date);
    }

    private static void viewWeeklySummary() {
        System.out.print("Enter start date (yyyy-mm-dd): ");
        LocalDate startDate = LocalDate.parse(scanner.nextLine());
        System.out.print("Enter end date (yyyy-mm-dd): ");
        LocalDate endDate = LocalDate.parse(scanner.nextLine());
        expenseManager.viewSummary("week", startDate, endDate);
    }

    private static void viewMonthlySummary() {
        System.out.print("Enter month (1-12): ");
        int month = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter year: ");
        int year = Integer.parseInt(scanner.nextLine());
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        expenseManager.viewSummary("month", startDate, endDate);
    }

    private static void exitApplication() {
        System.out.println("Thank you for using the Daily Expense Tracker. Goodbye!");
        System.exit(0);
    }
}

