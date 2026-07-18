package expensetracker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Expense {
    private final long id;
    private final String title;
    private final BigDecimal amount;
    private final String category;
    private final LocalDate date;
    private final String note;

    public Expense(long id, String title, BigDecimal amount, String category, LocalDate date, String note) {
        if (id <= 0) {
            throw new IllegalArgumentException("Expense id must be positive.");
        }

        this.title = requireText(title, "Title");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null.");
        this.category = requireText(category, "Category");
        this.date = Objects.requireNonNull(date, "Date cannot be null.");
        this.note = note == null ? "" : note.trim();
        this.id = id;

        if (this.amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    public boolean hasNote() {
        return !note.isBlank();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }

        return value.trim();
    }
}
