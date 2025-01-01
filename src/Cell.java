package assignments.ex2.src;

public class Cell {
    private String value;

    // Constructor: Initializes the cell's content.
    public Cell(String value) {
        this.value = value;
    }

    // Determines if the cell contains a valid number.
    public boolean isNumber() {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private boolean isNumber(String cellValue) {
        try {
            Double.parseDouble(cellValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Determines if the cell contains plain text.
    public boolean isText() {
        return !isNumber() && !isFormula();
    }

    // Checks if the cell contains a formula starting with '='.
    public boolean isFormula() {
        return value.startsWith("=");
    }

    // Computes the value of a formula, assuming it is valid.
    public double computeFormula(String formula, Spreadsheet spreadsheet) {
        String[] tokens = formula.split(" ");
        double result = 0;
        char operator = '+';

        for (String token : tokens) {
            if (token.matches("[A-Z]\\d+")) { // Check for cell references
                int[] pos = spreadsheet.getCellPosition(token);
                String cellValue = spreadsheet.eval(pos[0], pos[1]);
                if (!isNumber(cellValue)) {
                    throw new IllegalArgumentException("Invalid reference in formula: " + token);
                }
                double num = Double.parseDouble(cellValue);
                result = applyOperator(result, operator, num);
            } else if (token.matches("[+\\-*/]")) { // Check for operators
                operator = token.charAt(0);
            } else { // Assume it's a number
                double num = Double.parseDouble(token);
                result = applyOperator(result, operator, num);
            }
        }
        return result;
    }

    // Applies an operator to two operands and returns the result.
    private double applyOperator(double a, char operator, double b) {
        return switch (operator) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> a / b;
            default -> throw new IllegalArgumentException("Invalid operator: " + operator);
        };
    }

    // Getter for the value of the cell.
    public String getValue() {
        return value;
    }

    // Setter for the value of the cell.
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
