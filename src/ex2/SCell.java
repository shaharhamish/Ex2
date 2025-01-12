package assignments.ex2.src.ex2;

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
        formula = formula.replaceAll("\\s", "");
        if (formula.isEmpty()) return false;

        // Check for basic formula patterns
        if (formula.matches("^-?\\d+(\\.\\d+)?$")) return true;  // Simple number
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
            }
            else if (c == ')') {
                if (expectOperand) return false;
                parentheses--;
                if (parentheses < 0) return false;
                expectOperator = true;
                expectOperand = false;
            }
            else if ("+-*/".indexOf(c) >= 0) {
                if (expectOperand || !expectOperator) return false;
                expectOperator = false;
                expectOperand = true;
            }
            else if (Character.isLetterOrDigit(c) || c == '.') {
                if (!expectOperand) return false;
                while (i < formula.length() &&
                        (Character.isLetterOrDigit(formula.charAt(i)) ||
                                formula.charAt(i) == '.')) {
                    i++;
                }
                i--;
                expectOperator = true;
                expectOperand = false;
            }
            else {
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
        // If the cell is already evaluated or has an invalid formula, return
        if (isEvaluated || type == Ex2Utils.ERR_FORM_FORMAT) {
            return;
        }

        // If the cell is already being evaluated, it means we have a cycle
        if (isInCycle) {
            this.type = Ex2Utils.ERR_CYCLE_FORM;
            this.computedValue = 0;
            isEvaluated = true;
            return;
        }

        // Mark the cell as being evaluated to detect cycles
        isInCycle = true;

        // Evaluate the cell based on its content
        if (line.startsWith("=")) {
            try {
                // Evaluate the formula
                computedValue = evaluateFormula(line.substring(1), sheet);
                this.type = Ex2Utils.FORM;
            } catch (ArithmeticException | IllegalArgumentException e) {
                // Handle errors (e.g., division by zero, invalid formula)
                if (this.type != Ex2Utils.ERR_CYCLE_FORM) {
                    this.type = Ex2Utils.ERR_FORM_FORMAT;
                }
                this.computedValue = 0;
            }
        } else {
            try {
                // If the cell contains a number, parse it
                computedValue = Double.parseDouble(line);
                this.type = Ex2Utils.NUMBER;
            } catch (NumberFormatException e) {
                // If the cell contains text, mark it as TEXT
                this.type = Ex2Utils.TEXT;
                this.computedValue = 0;
            }
        }

        // Mark the cell as no longer being evaluated
        isInCycle = false;
        isEvaluated = true;
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

        // Check if the operand is a cell reference (e.g., "A1")
        if (operand.matches("[A-Z]+[0-9]+")) {
            int col = convertColumnToIndex(operand.replaceAll("[0-9]", ""));
            int row = Integer.parseInt(operand.replaceAll("[A-Z]", ""));

            // Check if the referenced cell is within bounds
            if (!sheet.isIn(col, row)) {
                throw new IllegalArgumentException("Invalid cell reference");
            }

            // Get the referenced cell
            SCell refCell = (SCell) sheet.get(col, row);
            if (refCell == this) {
                // Self-reference detected
                this.type = Ex2Utils.ERR_CYCLE_FORM;
                throw new IllegalArgumentException("Self reference detected");
            }

            if (refCell == null || refCell.getData().isEmpty()) {
                throw new IllegalArgumentException("Referenced cell is empty");
            }

            // Check if the referenced cell is already being evaluated (circular reference)
            if (refCell.isInCycle) {
                this.type = Ex2Utils.ERR_CYCLE_FORM;
                refCell.type = Ex2Utils.ERR_CYCLE_FORM;
                throw new IllegalArgumentException("Circular reference detected");
            }

            // Add the referenced cell as a dependency
            addDependentCell(refCell);

            // Evaluate the referenced cell
            refCell.evaluate(sheet);

            // If the referenced cell has an error, propagate the error
            if (refCell.getType() == Ex2Utils.ERR_CYCLE_FORM ||
                    refCell.getType() == Ex2Utils.ERR_FORM_FORMAT) {
                throw new IllegalArgumentException("Referenced cell has an error");
            }

            // Return the computed value of the referenced cell
            return refCell.getComputedValue();
        }

        // If the operand is a number, parse it
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
                return String.format("%.1f", computedValue);
            case Ex2Utils.FORM:
                return String.format("%.1f", computedValue);
            case Ex2Utils.ERR_FORM_FORMAT:
                return "ERR_FORM";
            case Ex2Utils.ERR_CYCLE_FORM:
                return "ERR_CYCL";
            default:
                return getData();
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