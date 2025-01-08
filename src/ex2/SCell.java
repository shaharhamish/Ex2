package assignments.ex2.src.ex2;

public class SCell implements Cell {
    private String line; // Raw data or formula in the cell
    private int type; // Type of the cell (e.g., text, number, formula)
    private double computedValue; // Evaluated numeric value of the cell
    private boolean isEvaluated = false; // Evaluation status of the cell
    private boolean isInCycle = false; // To detect cycles during evaluation
    private SCell[] dependentCells = new SCell[10]; // Array to track dependent cells
    private int dependentCount = 0; // Number of dependent cells
    private int colIndex; // Column index of the cell
    private int rowIndex; // Row index of the cell

    // Constructor to initialize the cell with given data
    public SCell(String s) {
        setData(s);
    }

    @Override
    public void setData(String s) {
        this.line = s.trim();
        this.isEvaluated = false; // Reset evaluation status
        this.isInCycle = false; // Reset cycle detection status

        // Determine the type of the cell based on its content
        if (line.equals("=")) {
            if (line.length() == 1)
                this.type = Ex2Utils.ERR_FORM_FORMAT; // Invalid formula when only "=" is present

        } else if (line.startsWith("=")) {
            if (line.length() == 1) {
                this.type = Ex2Utils.ERR_FORM_FORMAT; // Invalid formula
            } else {
                this.type = Ex2Utils.FORM; // Formula type
            }
        } else {
            try {
                this.computedValue = Double.parseDouble(line); // Attempt to parse as a number
                this.type = Ex2Utils.NUMBER;
            } catch (NumberFormatException e) {
                this.type = Ex2Utils.TEXT; // Treat as text if parsing fails
            }
        }
    }

    @Override
    public String getData() {
        return line; // Return the raw data or formula in the cell
    }

    @Override
    public int getType() {
        return type; // Return the type of the cell
    }

    @Override
    public void setType(int t) {
        this.type = t; // Update the cell type
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
            return; // Skip if already evaluated
        }

        // Check for cyclic references
        if (isInCycle) {
            System.err.println("Cyclic reference detected in cell: " + getReference());
            this.type = Ex2Utils.ERR_CYCLE_FORM; // Mark the cell as having a cycle error
            this.computedValue = 0;
            isEvaluated = true;
            return;
        }

        isInCycle = true; // Mark the cell as being evaluated

        if (line.equals("=")) {
            // Handle invalid formula (just "=")
            this.type = Ex2Utils.ERR_FORM_FORMAT;
            this.computedValue = 0; // No meaningful computed value
            isEvaluated = true;
            isInCycle = false;
            return;
        } else if (line.startsWith("=")) {
            try {
                computedValue = evaluateFormula(line.substring(1), sheet); // Evaluate the formula
                this.type = Ex2Utils.FORM; // Set as formula type
            } catch (ArithmeticException | IllegalArgumentException e) {
                this.type = Ex2Utils.ERR_FORM_FORMAT; // Handle invalid formulas or operations
                computedValue = 0;
            }
        } else {
            try {
                computedValue = Double.parseDouble(line); // Parse as a number
                this.type = Ex2Utils.NUMBER;
            } catch (NumberFormatException e) {
                this.type = Ex2Utils.TEXT; // Treat as text if parsing fails
                computedValue = 0;
            }
        }

        isInCycle = false; // Clear the cycle flag after evaluation
        isEvaluated = true; // Mark as evaluated
        sheet.eval();
    }


    // Evaluate a formula by removing spaces and processing the expression
    private double evaluateFormula(String formula, Ex2Sheet sheet) {
        formula = formula.replaceAll("\\s", ""); // Remove spaces
        return evaluateExpression(formula, sheet);
    }

    // Process a mathematical expression recursively
    private double evaluateExpression(String formula, Ex2Sheet sheet) {
        double result = 0.0; // Accumulated result of the expression
        String operator = "+"; // Current operator (default to addition)
        int i = 0;

        while (i < formula.length()) {
            char currentChar = formula.charAt(i);

            if (currentChar == '(') {
                // Handle sub-expressions within parentheses
                int closingParenthesisIndex = findClosingParenthesis(formula, i);
                String subExpression = formula.substring(i + 1, closingParenthesisIndex);
                double subResult = evaluateExpression(subExpression, sheet);
                result = applyOperation(result, subResult, operator);
                i = closingParenthesisIndex + 1; // Skip past the closing parenthesis
            } else if ("+-*/".indexOf(currentChar) >= 0) {
                // Update the operator for the next number
                operator = String.valueOf(currentChar);
                i++;
            } else {
                // Parse numbers or cell references
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

    // Find the closing parenthesis matching the opening one
    private int findClosingParenthesis(String formula, int openParenthesisIndex) {
        int depth = 1; // Track the depth of nested parentheses
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

    // Parse an operand (number or cell reference)
    private double parseOperand(String operand, Ex2Sheet sheet) {
        operand = operand.toUpperCase(); // Normalize to uppercase for consistency

        if (operand.matches("[A-Za-z]+[0-9]+")) { // Check if it's a cell reference
            int col = convertColumnToIndex(operand.replaceAll("[0-9]", "")); // Extract column
            int row = Integer.parseInt(operand.replaceAll("[A-Za-z]", "")); // Extract row

            if (!sheet.isIn(col, row)) {
                throw new IllegalArgumentException("Invalid cell reference: " + operand);
            }

            SCell refCell = (SCell) sheet.get(col, row);

            if (refCell == this || refCell.isInCycle) {
                // Detect self-references or cyclic dependencies
                System.err.println("Cyclic reference detected in cell: " + operand);
                throw new IllegalArgumentException("Cyclic reference detected: " + operand);
            }

            refCell.isInCycle = true; // Mark referenced cell as part of the cycle
            refCell.evaluate(sheet);
            refCell.isInCycle = false;

            addDependentCell(refCell); // Track dependency
            return refCell.getComputedValue();
        } else {
            try {
                return Double.parseDouble(operand); // Parse as a number
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid token in formula: " + operand);
            }
        }
    }

    // Apply an operation (+, -, *, /) to the current and new values
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

    // Convert a column label (e.g., "A") to a zero-based index
    private int convertColumnToIndex(String column) {
        int index = 0;
        for (int i = 0; i < column.length(); i++) {
            char c = column.charAt(i);
            index = index * 26 + (c - 'A' + 1);
        }
        return index - 1; // Convert to zero-based index
    }

    public double getComputedValue() {
        return computedValue; // Return the evaluated value
    }

    @Override
    public String toString() {
        if (type == Ex2Utils.NUMBER) {
            return String.format("%.1f", computedValue); // Format numbers with one decimal place
        }
        if (type == Ex2Utils.FORM) {
            return String.valueOf(computedValue); // Display computed value for formulas
        }
        if (type == Ex2Utils.ERR_FORM_FORMAT) {
            return "ERR_FORM"; // Explicitly display error for invalid formulas
        }
        if (type == Ex2Utils.ERR_CYCLE_FORM){
            return "ERR_CYCL";
        }
        return getData(); // Return raw data for text cells
    }


    // Add a dependent cell to the dependency list
    public void addDependentCell(SCell dependentCell) {
        if (dependentCount >= dependentCells.length) {
            SCell[] newDependentCells = new SCell[dependentCells.length * 2];
            System.arraycopy(dependentCells, 0, newDependentCells, 0, dependentCells.length);
            dependentCells = newDependentCells;
        }
        dependentCells[dependentCount++] = dependentCell;
    }

    // Set the position of the cell in the spreadsheet
    public void setPosition(int col, int row) {
        this.colIndex = col;
        this.rowIndex = row;
    }

    // Get the cell's reference (e.g., "A1")
    public String getReference() {
        StringBuilder colRef = new StringBuilder();
        int tempCol = colIndex + 1;
        while (tempCol > 0) {
            colRef.insert(0, (char) ('A' + (tempCol - 1) % 26));
            tempCol = (tempCol - 1) / 26;
        }
        return colRef.toString() + (rowIndex + 1);
    }
    public boolean hasDependencyOn(SCell other) {
        // Check if this cell depends on another cell
        if (line.startsWith("=")) {
            String formula = line.substring(1); // Remove '='
            return formula.contains(other.getReference()); // Simplified dependency check
        }
        return false;
    }
}
