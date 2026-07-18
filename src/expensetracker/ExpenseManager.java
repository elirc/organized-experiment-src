package expensetracker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExpenseManager {
    private final List<Expense> expenses = new ArrayList<>();
    private long nextId = 1;

    public void loadExpenses(Collection<Expense> loadedExpenses) {
        expenses.clear();
        expenses.addAll(loadedExpenses);
        nextId = calculateNextId();
    }

    public Expense addExpense(String title, String amountText, String category, String dateText, String note) {
        String cleanTitle = requireText(title, "Title");
        BigDecimal amount = parseAmount(amountText);
        String cleanCategory = normalizeCategory(category);
        LocalDate date = parseDate(dateText);
        String cleanNote = note == null ? "" : note.trim();

        Expense expense = new Expense(nextId, cleanTitle, amount, cleanCategory, date, cleanNote);
        expenses.add(expense);
        nextId++;
        return expense;
    }

    public List<Expense> getAllExpenses() {
        return new ArrayList<>(expenses);
    }

    public List<Expense> getExpensesByCategory(String category) {
        String requestedCategory = normalizeCategory(category);
        List<Expense> matches = new ArrayList<>();

        for (Expense expense : expenses) {
            if (expense.getCategory().equalsIgnoreCase(requestedCategory)) {
                matches.add(expense);
            }
        }

        return matches;
    }

    public List<Expense> getExpensesByMonth(String yearMonthText) {
        YearMonth requestedMonth = parseYearMonth(yearMonthText);
        List<Expense> matches = new ArrayList<>();

        for (Expense expense : expenses) {
            if (YearMonth.from(expense.getDate()).equals(requestedMonth)) {
                matches.add(expense);
            }
        }

        return matches;
    }

    public BigDecimal getTotalSpending() {
        BigDecimal total = BigDecimal.ZERO;

        for (Expense expense : expenses) {
            total = total.add(expense.getAmount());
        }

        return total;
    }

    public Map<String, BigDecimal> getSpendingByCategory() {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();

        for (Expense expense : expenses) {
            String category = expense.getCategory();
            BigDecimal currentTotal = totals.getOrDefault(category, BigDecimal.ZERO);
            totals.put(category, currentTotal.add(expense.getAmount()));
        }

        return totals;
    }

    private long calculateNextId() {
        long highestId = 0;

        for (Expense expense : expenses) {
            if (expense.getId() > highestId) {
                highestId = expense.getId();
            }
        }

        return highestId + 1;
    }

    private BigDecimal parseAmount(String amountText) {
        String value = requireText(amountText, "Amount");

        try {
            BigDecimal amount = new BigDecimal(value).setScale(2, RoundingMode.UNNECESSARY);

            if (amount.signum() <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero.");
            }

            return amount;
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Amount must use at most 2 decimal places.");
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Amount must be a valid number.");
        }
    }

    private LocalDate parseDate(String dateText) {
        String value = requireText(dateText, "Date");

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Date must use the format yyyy-MM-dd.");
        }
    }

    private YearMonth parseYearMonth(String yearMonthText) {
        String value = requireText(yearMonthText, "Month");

        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Month must use the format yyyy-MM.");
        }
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }

        return value.trim();
    }

    private String normalizeCategory(String category) {
        String trimmed = requireText(category, "Category");
        String[] words = trimmed.split("\\s+");
        StringBuilder normalized = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            normalized.append(word.substring(0, 1).toUpperCase());
            if (word.length() > 1) {
                normalized.append(word.substring(1).toLowerCase());
            }

            if (i < words.length - 1) {
                normalized.append(' ');
            }
        }

        return normalized.toString();
    }
}
