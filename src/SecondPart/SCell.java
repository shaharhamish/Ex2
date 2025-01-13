package assignments.ex2.src.SecondPart;

public class SCell implements Cell {
    // The raw content of the cell (text, number, or formula).
    private String line;

    // The type of the cell (NUMBER, TEXT, FORM, ERR_FORM_FORMAT, ERR_CYCLE_FORM).
    private int type;

    // The computed value of the cell (if applicable).
    private double computedValue;

    // Whether the cell has been evaluated.
    public boolean isEvaluated = false;

    // Whether the cell is part of a circular reference.
    private boolean isInCycle = false;

    // Cells that depend on this cell.
    private SCell[] dependentCells = new SCell[10];

    // Number of dependent cells.
    private int dependentCount = 0;

    // Column index of the cell.
    private int colIndex;

    // Row index of the cell.
    private int rowIndex;

    /**
     * Constructor that initializes the cell with a given raw content.
     * @param s The raw content for the cell (e.g., number, text, or formula).
     */
    public SCell(String s) {
        setData(s);
    }

    /**
     * Sets the data (raw content) of the cell and determines its type.
     * The content is parsed to identify whether it is a number, text, or formula.
     * @param s The raw content for the cell (e.g., number, text, or formula).
     */
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

    /**
     * Clears the evaluation state of the cell and all its dependent cells.
     * This method is useful when a cell's value changes from an error state to a valid state.
     */
    public void clearEvaluationState() {
        this.isEvaluated = false;
        for (int i = 0; i < dependentCount; i++) {
            SCell dependentCell = dependentCells[i];
            if (dependentCell != null) {
                dependentCell.clearEvaluationState();
            }
        }
    }

    /**
     * Validates whether a formula is correct by checking its syntax.
     * The formula is checked for valid operators, parentheses, and cell references.
     * @param formula The formula to validate.
     * @return true if the formula is valid, false otherwise.
     */
    private boolean isValidFormula(String formula) {
        formula = formula.replaceAll("\\s", ""); // Remove all whitespace
        if (formula.isEmpty()) return false;

        if (formula.startsWith("-")) {
            formula = formula.substring(1);
        }

        if (formula.matches("^-?\\d+(\\.\\d+)?$")) return true;
        if (formula.matches("^[A-Z]+\\d+$")) return true;

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

    /**
     * Returns the raw data (content) of the cell.
     * @return The raw content of the cell as a string.
     */
    @Override
    public String getData() {
        return line;
    }

    /**
     * Returns the type of the cell (e.g., number, text, formula).
     * @return The type of the cell.
     */
    @Override
    public int getType() {
        return type;
    }

    /**
     * Sets the type of the cell.
     * @param t The type to set for the cell (e.g., number, text, or formula).
     */
    @Override
    public void setType(int t) {
        this.type = t;
    }

    /**
     * Returns the order of the cell (default implementation returns 0).
     * @return The order of the cell.
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * Sets the order of the cell (no operation in default implementation).
     * @param t The order to set for the cell.
     */
    @Override
    public void setOrder(int t) {}

    /**
     * Evaluates the content of the cell.
     * If the cell contains a formula, it will be evaluated based on other cell values.
     * If there is a circular reference or formula error, the error is propagated.
     * @param sheet The sheet where the cell is located.
     */
    public void evaluate(Ex2Sheet sheet) {
        if (isEvaluated || type == Ex2Utils.ERR_FORM_FORMAT || type == Ex2Utils.ERR_CYCLE_FORM) {
            return;
        }

        if (isInCycle) {
            this.type = Ex2Utils.ERR_CYCLE_FORM;
            this.computedValue = 0;
            isEvaluated = true;
            propagateCycleError(); // Propagate the error to dependent cells
            return;
        }

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

        isInCycle = false;
        isEvaluated = true;
    }

    /**
     * Propagates the cycle error (ERR_CYCL) to all dependent cells.
     * This method is called when a circular reference is detected.
     */
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

    /**
     * Evaluates a formula by handling parentheses and operators.
     * @param formula The formula to evaluate.
     * @param sheet The sheet containing the cell references.
     * @return The result of the formula evaluation.
     */
    private double evaluateFormula(String formula, Ex2Sheet sheet) {
        formula = formula.replaceAll("\\s", "");
        return evaluateExpression(formula, sheet);
    }

    /**
     * Evaluates a mathematical expression, handling operators, parentheses, and cell references.
     * @param formula The formula to evaluate.
     * @param sheet The sheet containing the cell references.
     * @return The evaluated result of the formula.
     */
    private double evaluateExpression(String formula, Ex2Sheet sheet) {
        while (formula.contains("(")) {
            int start = formula.lastIndexOf("(");
            int end = findMatchingParenthesis(formula, start);
            if (end == -1) throw new IllegalArgumentException("Mismatched parentheses");

            String subExpr = formula.substring(start + 1, end);
            double value = evaluateExpression(subExpr, sheet);
            formula = formula.substring(0, start) + value + formula.substring(end + 1);
        }

        formula = formula.replaceAll("\\s", "");
        if (formula.startsWith("-")) {
            double value = evaluateExpression(formula.substring(1), sheet);
            return -value;
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

    /**
     * Finds the index of the matching closing parenthesis for an opening parenthesis.
     * @param expr The expression containing parentheses.
     * @param start The index of the opening parenthesis.
     * @return The index of the matching closing parenthesis.
     */
    private int findMatchingParenthesis(String expr, int start) {
        int count = 1;
        for (int i = start + 1; i < expr.length(); i++) {
            if (expr.charAt(i) == '(') count++;
            if (expr.charAt(i) == ')') count--;
            if (count == 0) return i;
        }
        return -1;
    }

    /**
     * Parses an operand (either a number or a cell reference).
     * @param operand The operand string to parse.
     * @param sheet The sheet containing the cell references.
     * @return The evaluated numeric value of the operand.
     */
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

            if (refCell.isInCycle) {
                this.type = Ex2Utils.ERR_CYCLE_FORM;
                refCell.type = Ex2Utils.ERR_CYCLE_FORM;
                throw new IllegalArgumentException("Circular reference detected");
            }

            addDependentCell(refCell);
            refCell.evaluate(sheet);

            // Check if referenced cell has ERR_CYCL and propagate it
            if (refCell.getType() == Ex2Utils.ERR_CYCLE_FORM) {
                this.type = Ex2Utils.ERR_CYCLE_FORM;
                this.isEvaluated = true;
                this.computedValue = 0;
                propagateCycleError();
                throw new IllegalArgumentException("Referenced cell has a cycle error");
            }

            if (refCell.getType() == Ex2Utils.ERR_FORM_FORMAT) {
                throw new IllegalArgumentException("Referenced cell has a formula error");
            }

            return refCell.getComputedValue();
        }

        try {
            return Double.parseDouble(operand);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value: " + operand);
        }
    }

    /**
     * Applies an arithmetic operation (+, -, *, /) to two values.
     * @param currentValue The current value before the operation.
     * @param newValue The new value to apply the operation to.
     * @param operator The operator to apply.
     * @return The result of the operation.
     */
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

    /**
     * Converts a column label (e.g., "A") to its zero-based index.
     * @param column The column label (e.g., "A").
     * @return The zero-based index of the column.
     */
    private int convertColumnToIndex(String column) {
        int index = 0;
        for (int i = 0; i < column.length(); i++) {
            index = index * 26 + (column.charAt(i) - 'A' + 1);
        }
        return index - 1;
    }

    /**
     * Returns the computed value of the cell.
     * @return The computed numeric value.
     */
    public double getComputedValue() {
        return computedValue;
    }

    /**
     * Returns a string representation of the cell.
     * @return A string representation of the cell (including error states).
     */
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

    /**
     * Formats the computed value of the cell to display up to 8 decimal places if necessary.
     * @return A string representation of the computed value.
     */
    private String formatComputedValue() {
        if (computedValue % 1 == 0) {
            return String.format("%.1f", computedValue);
        } else {
            return String.format("%.8f", computedValue).replaceAll("0*$", "").replaceAll("\\.$", "");
        }
    }

    /**
     * Adds a dependent cell to this cell's list of dependent cells.
     * @param dependentCell The cell that depends on this one.
     */
    public void addDependentCell(SCell dependentCell) {
        if (dependentCount >= dependentCells.length) {
            SCell[] newDependentCells = new SCell[dependentCells.length * 2];
            System.arraycopy(dependentCells, 0, newDependentCells, 0, dependentCells.length);
            dependentCells = newDependentCells;
        }
        dependentCells[dependentCount++] = dependentCell;
    }

    /**
     * Sets the position (row and column) of the cell in the spreadsheet.
     * @param col The column index (zero-based).
     * @param row The row index (zero-based).
     */
    public void setPosition(int col, int row) {
        this.colIndex = col;
        this.rowIndex = row;
    }

    /**
     * Returns the cell reference (e.g., "A1") for the current cell.
     * @return The cell reference.
     */
    public String getReference() {
        StringBuilder colRef = new StringBuilder();
        int tempCol = colIndex + 1;
        while (tempCol > 0) {
            colRef.insert(0, (char) ((tempCol - 1) % 26 + 'A'));
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