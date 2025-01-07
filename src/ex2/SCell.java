package assignments.ex2.src.ex2;

public class SCell implements Cell {
    private String line;
    private int type;
    private double computedValue;
    private boolean isEvaluated = false;
    private boolean isInCycle = false; // To track self-references
    private SCell[] dependentCells = new SCell[10]; // Track all dependent cells with an array (fixed size for simplicity)
    private int dependentCount = 0; // Keep track of how many dependents are in the array

    public SCell(String s) {
        setData(s);
    }

    @Override
    public void setData(String s) {
        this.line = s.trim();
        this.isEvaluated = false; // Reset evaluation status
        this.isInCycle = false; // Reset cycle status

        if (line.equals("=")) {
            // Case where the formula is just "="
            this.type = Ex2Utils.ERR_FORM_FORMAT; // Invalid formula
            this.computedValue = 0; // Set value to 0 for consistency
        } else if (line.startsWith("=")) {
            if (line.length() == 1) {
                this.type = Ex2Utils.ERR_FORM_FORMAT; // Invalid formula
            } else {
                this.type = Ex2Utils.FORM;
            }
        } else {
            try {
                this.computedValue = Double.parseDouble(line);
                this.type = Ex2Utils.NUMBER;
            } catch (NumberFormatException e) {
                this.type = Ex2Utils.TEXT;
            }
        }
    }

    @Override
    public String getData() {
        return line;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        this.type = t;
    }

    @Override
    public int getOrder() {
        return 0; // Not used in this implementation
    }

    @Override
    public void setOrder(int t) {
        // Not used in this implementation
    }

    public void evaluate(Ex2Sheet sheet) {
        if (isEvaluated) {
            return;
        }

        if (line.startsWith("=")) {
            try {
                computedValue = evaluateFormula(line.substring(1), sheet);
                isEvaluated = true;

                // After evaluating the current cell, trigger evaluation for dependent cells
                for (int i = 0; i < dependentCount; i++) {
                    dependentCells[i].evaluate(sheet);
                }
            } catch (IllegalArgumentException e) {
                this.type = Ex2Utils.ERR_FORM_FORMAT;
                computedValue = 0;
            } catch (ArithmeticException e) {
                this.type = Ex2Utils.ERR_FORM_FORMAT; // Handle division by zero or invalid operations
                computedValue = 0;
            }
        } else {
            try {
                computedValue = Double.parseDouble(line);
                type = Ex2Utils.NUMBER;
            } catch (NumberFormatException e) {
                computedValue = 0;
                type = Ex2Utils.TEXT;
            }
        }
    }

    private double evaluateFormula(String formula, Ex2Sheet sheet) {
        formula = formula.replaceAll("\\s", ""); // Remove spaces
        return evaluateExpression(formula, sheet);
    }

    private double evaluateExpression(String formula, Ex2Sheet sheet) {
        double result = 0.0;
        String operator = "+";
        int i = 0;

        while (i < formula.length()) {
            char currentChar = formula.charAt(i);

            if (currentChar == '(') {
                // Start of a sub-expression: find the corresponding closing parenthesis
                int closingParenthesisIndex = findClosingParenthesis(formula, i);
                String subExpression = formula.substring(i + 1, closingParenthesisIndex);
                double subResult = evaluateExpression(subExpression, sheet);
                result = applyOperation(result, subResult, operator);
                i = closingParenthesisIndex + 1; // Skip past the closing parenthesis
            } else if ("+-*/".indexOf(currentChar) >= 0) {
                // Operator: set the operator for the next number
                operator = String.valueOf(currentChar);
                i++;
            } else {
                // Number or cell reference: parse the operand
                StringBuilder operand = new StringBuilder();
                while (i < formula.length() && (Character.isDigit(formula.charAt(i)) || formula.charAt(i) == '.' || Character.isLetter(formula.charAt(i)))) {
                    operand.append(formula.charAt(i));
                    i++;
                }
                double value = parseOperand(operand.toString(), sheet);
                result = applyOperation(result, value, operator);
            }
        }
        return result;
    }

    private int findClosingParenthesis(String formula, int openParenthesisIndex) {
        int depth = 1;
        int i = openParenthesisIndex + 1;
        while (i < formula.length() && depth > 0) {
            char currentChar = formula.charAt(i);
            if (currentChar == '(') {
                depth++;
            } else if (currentChar == ')') {
                depth--;
            }
            i++;
        }
        return i - 1; // Return the index of the closing parenthesis
    }

    private double parseOperand(String operand, Ex2Sheet sheet) {
        double value = 0.0;
        // Normalize the cell reference to uppercase (e.g., "a0" becomes "A0")
        operand = operand.toUpperCase();

        if (operand.matches("[A-Za-z]+[0-9]+")) { // Cell reference (e.g., A1)
            int col = convertColumnToIndex(operand.replaceAll("[0-9]", ""));
            int row = Integer.parseInt(operand.replaceAll("[A-Za-z]", ""));

            if (!sheet.isIn(col, row)) {
                throw new IllegalArgumentException("Invalid cell reference: " + operand);
            }

            SCell refCell = (SCell) sheet.get(col, row);
            if (refCell == this) {
                throw new IllegalArgumentException("Self-referencing cell detected: " + operand);
            }

            // Detect cyclic references
            if (refCell.isInCycle) {
                throw new IllegalArgumentException("Cyclic reference detected: " + operand);
            }

            refCell.isInCycle = true; // Mark this cell as part of the cycle
            refCell.evaluate(sheet);
            value = refCell.getComputedValue();
            refCell.isInCycle = false; // Unmark after evaluation

            // Add this cell as a dependent of the reference
            refCell.addDependentCell(this);
        } else {
            try {
                value = Double.parseDouble(operand);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid token in formula: " + operand);
            }
        }
        return value;
    }

    private double applyOperation(double currentValue, double newValue, String operator) {
        switch (operator) {
            case "+":
                return currentValue + newValue;
            case "-":
                return currentValue - newValue;
            case "*":
                return currentValue * newValue;
            case "/":
                if (newValue == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return currentValue / newValue;
            default:
                return newValue;
        }
    }

    // Converts a column letter (e.g., "A", "B", "C", ...) to a zero-based index (0, 1, 2, ...)
    private int convertColumnToIndex(String column) {
        int index = 0;
        for (int i = 0; i < column.length(); i++) {
            char c = column.charAt(i);
            index = index * 26 + (c - 'A' + 1); // Convert letter to its position in the alphabet
        }
        return index - 1; // Convert to zero-based index
    }

    public double getComputedValue() {
        return computedValue;
    }

    @Override
    public String toString() {
        if (type == Ex2Utils.NUMBER) {
            return String.format("%.1f", computedValue); // Format numeric values as doubles with 1 decimal place
        }
        if (type == Ex2Utils.FORM) {
            return String.valueOf(computedValue);
        }
        return getData();
    }

    // Add this cell as a dependent of another cell
    public void addDependentCell(SCell dependentCell) {
        if (dependentCount < dependentCells.length) {
            dependentCells[dependentCount++] = dependentCell;
        } else {
            // Resize the array if needed (or handle an overflow case)
            SCell[] newDependentCells = new SCell[dependentCells.length * 2];
            System.arraycopy(dependentCells, 0, newDependentCells, 0, dependentCells.length);
            dependentCells = newDependentCells;
            dependentCells[dependentCount++] = dependentCell;
        }
    }
}