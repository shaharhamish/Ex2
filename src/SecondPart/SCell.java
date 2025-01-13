package assignments.ex2.src.SecondPart;

public class SCell implements Cell {
    private String line; // The raw content of the cell
    private int type; // The type of the cell (NUMBER, TEXT, FORM, ERR_FORM_FORMAT, ERR_CYCLE_FORM)
    private double computedValue; // The computed value of the cell (if applicable)
    public boolean isEvaluated = false; // Whether the cell has been evaluated
    private boolean isInCycle = false; // Whether the cell is part of a circular reference
    private SCell[] dependentCells = new SCell[10]; // Cells that depend on this cell
    private int dependentCount = 0; // Number of dependent cells
    private int colIndex; // Column index of the cell
    private int rowIndex; // Row index of the cell

    // Constructor
    public SCell(String s) {
        setData(s);
    }

    // Set the data of the cell
    @Override
    public void setData(String s) {
        this.line = s.trim();
        this.isEvaluated = false;
        this.isInCycle = false;

        if (line.isEmpty()) {
            this.type = Ex2Utils.TEXT;
            return;
        }

        if (line.startsWith("=")) {
            if (isValidFormula(line.substring(1))) {
                this.type = Ex2Utils.FORM;
            } else {
                this.type = Ex2Utils.ERR_FORM_FORMAT;
            }
            return;
        }

        try {
            this.computedValue = Double.parseDouble(line);
            this.type = Ex2Utils.NUMBER;
        } catch (NumberFormatException e) {
            this.type = Ex2Utils.TEXT;
        }
    }

    // Check if a formula is valid
    private boolean isValidFormula(String formula) {
        formula = formula.replaceAll("\\s", ""); // Remove all whitespace
        if (formula.isEmpty()) return false;

        // Check for unary minus at the beginning
        if (formula.startsWith("-")) {
            formula = formula.substring(1); // Remove the unary minus and check the rest
        }

        // Check for basic formula patterns
        if (formula.matches("^-?\\d+(\\.\\d+)?$")) return true;  // Simple number (with optional unary minus)
        if (formula.matches("^[A-Z]+\\d+$")) return true;        // Cell reference

        // Check parentheses balance and operators
        int parentheses = 0;
        boolean expectOperator = false;
        boolean expectOperand = true;

        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);

            if (c == '(') {
                if (!expectOperand) return false;
                parentheses++;
                expectOperand = true;
            } else if (c == ')') {
                if (expectOperand) return false;
                parentheses--;
                if (parentheses < 0) return false;
                expectOperator = true;
                expectOperand = false;
            } else if ("+-*/".indexOf(c) >= 0) {
                if (expectOperand || !expectOperator) return false;
                expectOperator = false;
                expectOperand = true;
            } else if (Character.isLetterOrDigit(c) || c == '.') {
                if (!expectOperand) return false;
                while (i < formula.length() &&
                        (Character.isLetterOrDigit(formula.charAt(i)) ||
                                formula.charAt(i) == '.')) {
                    i++;
                }
                i--;
                expectOperator = true;
                expectOperand = false;
            } else {
                return false;
            }
        }

        return parentheses == 0 && !expectOperand;
    }

    // Get the raw data of the cell
    @Override
    public String getData() {
        return line;
    }

    // Get the type of the cell
    @Override
    public int getType() {
        return type;
    }

    // Set the type of the cell
    @Override
    public void setType(int t) {
        this.type = t;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void setOrder(int t) {
    }

    // Evaluate the cell
    public void evaluate(Ex2Sheet sheet) {
        if (isEvaluated || type == Ex2Utils.ERR_FORM_FORMAT) {
            return;
        }

        // If the cell is already being evaluated, it's part of a cycle
        if (isInCycle) {
            this.type = Ex2Utils.ERR_CYCLE_FORM;
            this.computedValue = 0;
            isEvaluated = true;
            propagateCycleError(); // Propagate the error to dependent cells
            return;
        }

        // Mark the cell as being evaluated
        isInCycle = true;

        if (line.startsWith("=")) {
            try {
                computedValue = evaluateFormula(line.substring(1), sheet);
                this.type = Ex2Utils.FORM;
            } catch (ArithmeticException | IllegalArgumentException e) {
                if (this.type != Ex2Utils.ERR_CYCLE_FORM) {
                    this.type = Ex2Utils.ERR_FORM_FORMAT;
                }
                this.computedValue = 0;
            }
        } else {
            try {
                computedValue = Double.parseDouble(line);
                this.type = Ex2Utils.NUMBER;
            } catch (NumberFormatException e) {
                this.type = Ex2Utils.TEXT;
                this.computedValue = 0;
            }
        }

        // Mark the cell as evaluated and no longer in a cycle
        isInCycle = false;
        isEvaluated = true;
    }

    // Propagate ERR_CYCL to all dependent cells
    private void propagateCycleError() {
        for (int i = 0; i < dependentCount; i++) {
            SCell dependentCell = dependentCells[i];
            if (dependentCell != null && dependentCell.getType() != Ex2Utils.ERR_CYCLE_FORM) {
                dependentCell.type = Ex2Utils.ERR_CYCLE_FORM;
                dependentCell.computedValue = 0;
                dependentCell.isEvaluated = true;
                dependentCell.propagateCycleError(); // Recursively propagate the error
            }
        }
    }

    // Evaluate a formula
    private double evaluateFormula(String formula, Ex2Sheet sheet) {
        formula = formula.replaceAll("\\s", "");
        return evaluateExpression(formula, sheet);
    }

    // Evaluate an expression
    private double evaluateExpression(String formula, Ex2Sheet sheet) {
        // Handle parentheses first
        while (formula.contains("(")) {
            int start = formula.lastIndexOf("(");
            int end = findMatchingParenthesis(formula, start);
            if (end == -1) throw new IllegalArgumentException("Mismatched parentheses");

            String subExpr = formula.substring(start + 1, end);
            double value = evaluateExpression(subExpr, sheet);
            formula = formula.substring(0, start) + value + formula.substring(end + 1);
        }

        // Handle unary minus
        formula = formula.replaceAll("\\s", ""); // Remove all whitespace
        if (formula.startsWith("-")) {
            // If the formula starts with a minus, treat it as a unary minus
            double value = evaluateExpression(formula.substring(1), sheet);
            return -value;
        }

        // Handle binary operations
        double result = 0.0;
        String operator = "+";
        int i = 0;

        while (i < formula.length()) {
            char currentChar = formula.charAt(i);

            if ("+-*/".indexOf(currentChar) >= 0) {
                operator = String.valueOf(currentChar);
                i++;
            } else {
                StringBuilder operand = new StringBuilder();
                while (i < formula.length() &&
                        (Character.isDigit(formula.charAt(i)) ||
                                Character.isLetter(formula.charAt(i)) ||
                                formula.charAt(i) == '.')) {
                    operand.append(formula.charAt(i++));
                }
                double value = parseOperand(operand.toString(), sheet);
                result = applyOperation(result, value, operator);
            }
        }
        return result;
    }

    // Find the matching parenthesis
    private int findMatchingParenthesis(String expr, int start) {
        int count = 1;
        for (int i = start + 1; i < expr.length(); i++) {
            if (expr.charAt(i) == '(') count++;
            if (expr.charAt(i) == ')') count--;
            if (count == 0) return i;
        }
        return -1;
    }

    // Parse an operand (either a number or a cell reference)
    private double parseOperand(String operand, Ex2Sheet sheet) {
        operand = operand.toUpperCase();

        if (operand.matches("[A-Z]+[0-9]+")) {
            int col = convertColumnToIndex(operand.replaceAll("[0-9]", ""));
            int row = Integer.parseInt(operand.replaceAll("[A-Z]", ""));

            if (!sheet.isIn(col, row)) {
                throw new IllegalArgumentException("Invalid cell reference");
            }

            SCell refCell = (SCell) sheet.get(col, row);
            if (refCell == this) {
                this.type = Ex2Utils.ERR_CYCLE_FORM;
                throw new IllegalArgumentException("Self reference detected");
            }

            if (refCell == null || refCell.getData().isEmpty()) {
                throw new IllegalArgumentException("Referenced cell is empty");
            }

            // If the referenced cell is already being evaluated, it's a circular reference
            if (refCell.isInCycle) {
                this.type = Ex2Utils.ERR_CYCLE_FORM;
                refCell.type = Ex2Utils.ERR_CYCLE_FORM;
                throw new IllegalArgumentException("Circular reference detected");
            }

            addDependentCell(refCell);
            refCell.evaluate(sheet);

            if (refCell.getType() == Ex2Utils.ERR_CYCLE_FORM ||
                    refCell.getType() == Ex2Utils.ERR_FORM_FORMAT) {
                throw new IllegalArgumentException("Referenced cell has an error");
            }

            return refCell.getComputedValue();
        }

        try {
            return Double.parseDouble(operand);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value: " + operand);
        }
    }

    // Apply an arithmetic operation
    private double applyOperation(double currentValue, double newValue, String operator) {
        switch (operator) {
            case "+": return currentValue + newValue;
            case "-": return currentValue - newValue;
            case "*": return currentValue * newValue;
            case "/":
                if (newValue == 0) throw new ArithmeticException("Division by zero");
                return currentValue / newValue;
            default: return newValue;
        }
    }

    // Convert a column label (e.g., "A") to its zero-based index
    private int convertColumnToIndex(String column) {
        int index = 0;
        for (int i = 0; i < column.length(); i++) {
            index = index * 26 + (column.charAt(i) - 'A' + 1);
        }
        return index - 1;
    }

    // Get the computed value of the cell
    public double getComputedValue() {
        return computedValue;
    }

    // Convert the cell to a string representation
    @Override
    public String toString() {
        switch (type) {
            case Ex2Utils.NUMBER:
            case Ex2Utils.FORM:
                return formatComputedValue();
            case Ex2Utils.ERR_FORM_FORMAT:
                return "ERR_FORM";
            case Ex2Utils.ERR_CYCLE_FORM:
                return "ERR_CYCL";
            default:
                return getData();
        }
    }

    // Format the computed value to show up to 8 decimal places if necessary
    private String formatComputedValue() {
        if (computedValue % 1 == 0) {
            return String.format("%.1f", computedValue);
        } else {
            return String.format("%.8f", computedValue).replaceAll("0*$", "").replaceAll("\\.$", "");
        }
    }

    // Add a dependent cell
    public void addDependentCell(SCell dependentCell) {
        if (dependentCount >= dependentCells.length) {
            SCell[] newDependentCells = new SCell[dependentCells.length * 2];
            System.arraycopy(dependentCells, 0, newDependentCells, 0, dependentCells.length);
            dependentCells = newDependentCells;
        }
        dependentCells[dependentCount++] = dependentCell;
    }

    // Set the position of the cell
    public void setPosition(int col, int row) {
        this.colIndex = col;
        this.rowIndex = row;
    }

    // Get the cell reference (e.g., "A1")
    public String getReference() {
        StringBuilder colRef = new StringBuilder();
        int tempCol = colIndex + 1;
        while (tempCol > 0) {
            colRef.insert(0, (char) ('A' + (tempCol - 1) % 26));
            tempCol = (tempCol - 1) / 26;
        }
        return colRef.toString() + (rowIndex + 1);
    }

    // Check if this cell depends on another cell
    public boolean hasDependencyOn(SCell other) {
        if (line.startsWith("=")) {
            String formula = line.substring(1);
            return formula.contains(other.getReference());
        }
        return false;
    }
}