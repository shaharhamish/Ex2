package assignments.ex2.src.ex2;

import java.util.Set;

public class SCell implements Cell {
    private String line;
    private int type;
    private double computedValue;
    private boolean isEvaluated = false; // Flag to indicate if the cell has been evaluated

    public SCell(String s) {
        setData(s);
    }

    @Override
    public void setData(String s) {
        this.line = s.trim();
        if (s.startsWith("=")) {
            this.type = Ex2Utils.FORM; // Formula
        } else {
            try {
                Double.parseDouble(s);
                this.type = Ex2Utils.NUMBER; // Number
            } catch (NumberFormatException e) {
                this.type = Ex2Utils.TEXT; // Text
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
        return 0;
    }

    @Override
    public void setOrder(int t) {}

    public void evaluate(Ex2Sheet sheet, Set<String> evaluationStack) {
        if (isEvaluated) {
            return; // Skip if already evaluated
        }

        if (evaluationStack.contains(line)) {
            throw new IllegalArgumentException("Circular reference detected in formula: " + line);
        }

        if (line.startsWith("=")) { // Formula handling
            evaluationStack.add(line);
            computedValue = evaluateFormula(sheet, line.substring(1), evaluationStack); // Evaluate the formula
            line = String.valueOf(computedValue); // Update the line with the computed value
            isEvaluated = true;
            evaluationStack.remove(line);
        } else {
            try {
                computedValue = Double.parseDouble(line); // Try parsing as a number
                type = Ex2Utils.NUMBER;
            } catch (NumberFormatException e) {
                type = Ex2Utils.TEXT; // Default to TEXT if parsing fails
            }
        }
    }

    /**
     * Evaluates a formula, handling both arithmetic and cell references.
     *
     * @param sheet           The spreadsheet containing the cell.
     * @param formula         The formula string to evaluate (without the leading '=' symbol).
     * @param evaluationStack Tracks visited cells to detect cycles.
     * @return The result of the formula evaluation.
     */
    private Double evaluateFormula(Ex2Sheet sheet, String formula, Set<String> evaluationStack) {
        // If it's a simple arithmetic expression or just a value, compute it directly
        if (formula.matches("[0-9\\+\\-\\*/\\.]+")) {
            return evaluateArithmeticExpression(formula);
        }

        try {
            // Delegate the actual formula computation to the computeFormula method
            return computeFormula(formula, sheet, evaluationStack);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error evaluating formula: " + formula, e);
        }
    }

    /**
     * Processes a single token in a formula.
     *
     * @param sheet           The spreadsheet containing the cell.
     * @param token           The token to process (either a number or a cell reference).
     * @param operator        The operator to apply.
     * @param evaluationStack Tracks visited cells to detect cycles.
     * @return The result of applying the operator to the token.
     */
    private double processToken(Ex2Sheet sheet, String token, String operator, Set<String> evaluationStack) {
        double result = 0.0;
        if (token.matches("[A-Za-z]+[0-9]+")) { // Cell reference
            Cell refCell = sheet.get(token);
            if (refCell != null && refCell instanceof SCell) {
                ((SCell) refCell).evaluate(sheet, evaluationStack); // Evaluate the referenced cell
                result = applyOperation(result, ((SCell) refCell).getComputedValue(), operator);
            } else {
                throw new IllegalArgumentException("Invalid or empty referenced cell: " + token);
            }
        } else {
            try {
                double value = Double.parseDouble(token); // Parse as number
                result = applyOperation(result, value, operator);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid token in formula: " + token);
            }
        }
        return result;
    }

    /**
     * Evaluates a basic arithmetic expression (supports +, -, *, /).
     *
     * @param formula The arithmetic formula string.
     * @return The result of the expression.
     */
    private double evaluateArithmeticExpression(String formula) {
        double result = 0.0;
        String[] tokens = formula.split("(?=[-+*/])|(?<=[-+*/])"); // Split by operators

        String operator = "+";
        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            if ("+-*/".contains(token)) {
                operator = token;
            } else {
                double value = Double.parseDouble(token);
                result = applyOperation(result, value, operator);
            }
        }
        return result;
    }

    /**
     * Computes the result of a formula.
     *
     * @param formula The formula to compute (without the '=' symbol).
     * @param sheet The spreadsheet to reference cell values.
     * @param evaluationStack Tracks visited cells to prevent cycles.
     * @return The computed result as a double.
     * @throws Exception if the formula is invalid.
     */
    private double computeFormula(String formula, Ex2Sheet sheet, Set<String> evaluationStack) throws Exception {
        formula = formula.trim();

        // If the formula is just a number without operations, return it directly
        if (formula.matches("[0-9]+(\\.[0-9]+)?")) {
            return Double.parseDouble(formula);
        }

        String[] tokens = formula.split("\\s*([+\\-*/])\\s*"); // Split by operators while keeping operators
        double result = 0;
        String operation = "+"; // Default operator to add

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            if ("+-*/".contains(token)) {
                operation = token; // Store the operator
            } else if (token.matches("[A-Za-z]+[0-9]+")) { // Cell reference
                Cell referencedCell = sheet.get(token);
                if (referencedCell == null || referencedCell.getType() == Ex2Utils.TEXT) {
                    throw new IllegalArgumentException("Invalid cell reference: " + token);
                }

                SCell referencedSCell = (SCell) referencedCell;
                referencedSCell.evaluate(sheet, evaluationStack); // Evaluate the referenced cell
                double value = referencedSCell.getComputedValue();
                result = applyOperation(result, value, operation); // Apply the operator
            } else { // Numeric value
                double value = Double.parseDouble(token);
                result = applyOperation(result, value, operation); // Apply the operator
            }
        }

        return result;
    }

    /**
     * Applies the given operation to two operands.
     *
     * @param currentValue The current accumulated value.
     * @param newValue The value to be combined.
     * @param operation The operation to apply (+, -, *, /).
     * @return The result after applying the operation.
     */
    private double applyOperation(double currentValue, double newValue, String operation) {
        switch (operation) {
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
                return newValue; // If no operation is defined, just return the new value
        }
    }

    /**
     * Returns the computed value of the cell.
     *
     * @return The computed value of the cell.
     */
    public double getComputedValue() {
        return computedValue;
    }

    @Override
    public String toString() {
        return getData();
    }
}
