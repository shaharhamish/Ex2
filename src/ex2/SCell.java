package assignments.ex2.src.ex2;

public class SCell implements Cell {
    private String line;
    private int type;  // Cell type (0 = Text, 1 = Number, 2 = Formula)

    public SCell(String s) {
        setData(s);  // Initialize the cell with data (formula, number, or text)
    }

    @Override
    public int getOrder() {
        return 0; // For simplicity, return 0 (no custom ordering logic required)
    }

    @Override
    public String toString() {
        return getData(); // Return string representation of the cell
    }

    @Override
    public void setData(String s) {
        line = s;  // Set the data
        determineType(); // Determine the type based on the data
    }

    @Override
    public String getData() {
        return line;  // Return the cell's data as a string
    }

    @Override
    public int getType() {
        return type;  // Return the type (Text, Number, Formula)
    }

    @Override
    public void setType(int t) {
        type = t;  // Set the type of the cell (Text, Number, Formula)
    }

    @Override
    public void setOrder(int t) {
        // Not needed for now, but we could implement ordering if required
    }

    // Method to determine the type of the cell: Text, Number, or Formula
    private void determineType() {
        if (line.startsWith("=")) {
            type = 2; // Formula
        } else if (isNumber(line)) {
            type = 1; // Number
        } else {
            type = 0; // Text
        }
    }

    // Utility method to check if a string is a valid number
    private boolean isNumber(String str) {
        try {
            Double.parseDouble(str);  // Try parsing it as a number
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

