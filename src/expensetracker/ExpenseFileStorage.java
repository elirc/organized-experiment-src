package expensetracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseFileStorage {
    private final Path filePath;

    public ExpenseFileStorage(Path filePath) {
        this.filePath = filePath;
    }

    public List<Expense> load() throws IOException {
        List<Expense> expenses = new ArrayList<>();

        if (Files.notExists(filePath)) {
            return expenses;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmedLine = line.trim();

                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    continue;
                }

                try {
                    expenses.add(parseLine(trimmedLine));
                } catch (IllegalArgumentException ex) {
                    System.out.println("Skipping invalid saved record on line " + lineNumber + ": " + ex.getMessage());
                }
            }
        }

        return expenses;
    }

    public void save(List<Expense> expenses) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("# id\ttitle\tamount\tcategory\tdate\tnote");
            writer.newLine();

            for (Expense expense : expenses) {
                writer.write(toLine(expense));
                writer.newLine();
            }
        }
    }

    private String toLine(Expense expense) {
        return expense.getId()
                + "\t" + escape(expense.getTitle())
                + "\t" + expense.getAmount().toPlainString()
                + "\t" + escape(expense.getCategory())
                + "\t" + expense.getDate()
                + "\t" + escape(expense.getNote());
    }

    private Expense parseLine(String line) {
        List<String> fields = splitPreservingEscapes(line);

        if (fields.size() != 6) {
            throw new IllegalArgumentException("Expected 6 fields but found " + fields.size() + ".");
        }

        try {
            long id = Long.parseLong(fields.get(0));
            String title = unescape(fields.get(1));
            BigDecimal amount = new BigDecimal(fields.get(2));
            String category = unescape(fields.get(3));
            LocalDate date = LocalDate.parse(fields.get(4));
            String note = unescape(fields.get(5));
            return new Expense(id, title, amount, category, date, note);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not parse expense record.");
        }
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String unescape(String value) {
        StringBuilder result = new StringBuilder();
        boolean escaping = false;

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);

            if (escaping) {
                switch (current) {
                    case 't' -> result.append('\t');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case '\\' -> result.append('\\');
                    default -> result.append(current);
                }
                escaping = false;
            } else if (current == '\\') {
                escaping = true;
            } else {
                result.append(current);
            }
        }

        if (escaping) {
            result.append('\\');
        }

        return result.toString();
    }

    // This prevents escaped tab characters from being treated as separators.
    private List<String> splitPreservingEscapes(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        boolean escaping = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);

            if (escaping) {
                currentPart.append('\\').append(current);
                escaping = false;
                continue;
            }

            if (current == '\\') {
                escaping = true;
                continue;
            }

            if (current == '\t') {
                parts.add(currentPart.toString());
                currentPart.setLength(0);
            } else {
                currentPart.append(current);
            }
        }

        if (escaping) {
            currentPart.append('\\');
        }

        parts.add(currentPart.toString());
        return parts;
    }
}
