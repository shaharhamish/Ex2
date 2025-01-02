package assignments.ex2.src.ex2;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a single cell in the spreadsheet.
 * Supports formulas, numbers, and text. Handles evaluation and type detection.
 */
public class SCell implements Cell {
    private String data; // The raw data stored in the cell (text, number, or formula)
    private int type; // The type of the cell (e.g., TEXT, NUMBER, FORMULA)
    private boolean isEvaluated = false; // Flag to indicate if the cell has been evaluated
    private Double computedValue = null; // The result of evaluating the cell (if applicable)
    private int order = 0; // The order of dependency (for formulas referencing other cells)

    /**
     * Constructs a new SCell with the given data.
     * Default type is TEXT.
     * @param data The data to store in the cell.
     */
    public SCell(String data) {
        this.data = data;
        this.type = Ex2Utils.TEXT; // Default type is TEXT
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public void setData(String data) {
        this.data = data;
        this.isEvaluated = false; // Reset evaluation flag when data changes
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Evaluates the cell's value. Handles formulas recursively, detecting circular references.
     * @param sheet The spreadsheet containing the cell.
     * @param evaluationStack Tracks visited cells to detect cycles.
     */
    public void evaluate(Ex2Sheet sheet, Set<String> evaluationStack) {
        if (isEvaluated) {
            return; // Skip if already evaluated
        }

        if (evaluationStack.contains(data)) {
            throw new IllegalArgumentException("Circular reference detected in formula: " + data);
        }

        if (data.startsWith("=")) { // Formula handling
            evaluationStack.add(data);
            computedValue = evaluateFormula(sheet, data.substring(1), evaluationStack); // Evaluate the formula
            isEvaluated = true;
            evaluationStack.remove(data);
        } else {
            try {
                computedValue = Double.parseDouble(data); // Try parsing as a number
                type = Ex2Utils.NUMBER;
            } catch (NumberFormatException e) {
                type = Ex2Utils.TEXT; // Default to TEXT if parsing fails
            }
        }
    }

    /**
     * Evaluates a formula, handling both arithmetic and cell references.
     * @param sheet The spreadsheet containing the cell.
     * @param formula The formula string to evaluate (without the leading '=').
     * @param evaluationStack Tracks visited cells to detect cycles.
     * @return The result of the formula evaluation.
     */
    private Double evaluateFormula(Ex2Sheet sheet, String formula, Set<String> evaluationStack) {
        if (formula.matches("[0-9\\+\\-\\*/\\.]+")) { // Simple arithmetic expressions
            return evaluateArithmeticExpression(formula);
        }

        double result = 0.0;
        String operator = "+"; // Default operator is addition

        // Remove spaces for simpler processing
        formula = formula.replaceAll("\\s+", "");

        // Tokenize the formula manually
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);

            // If character is an operator, process the previous token and update the operator
            if ("+-*/".indexOf(c) != -1) {
                if (token.length() > 0) {
                    result = processToken(sheet, token.toString(), operator, evaluationStack);
                    token.setLength(0); // Reset token builder
                }
                operator = String.valueOf(c); // Update operator
            } else {
                token.append(c); // Append character to token
            }
        }

        // Process the last token
        if (token.length() > 0) {
            result = processToken(sheet, token.toString(), operator, evaluationStack);
        }

        return result;
    }

    /**
     * Processes a single token in a formula.
     * @param sheet The spreadsheet containing the cell.
     * @param token The token to process (either a number or a cell reference).
     * @param operator The operator to apply.
     * @param evaluationStack Tracks visited cells to detect cycles.
     * @return The result of applying the operator to the token.
     */
    private double processToken(Ex2Sheet sheet, String token, String operator, Set<String> evaluationStack) {
        double result = 0.0;
        if (token.matches("[A-Za-z]+[0-9]+")) { // Cell reference
            Cell refCell = sheet.get(token);
            if (refCell != null && refCell instanceof SCell) {
                ((SCell) refCell).evaluate(sheet, evaluationStack); // Evaluate the referenced cell
                result = applyOperator(result, ((SCell) refCell).getComputedValue(), operator);
            }
        } else {
            try {
                double value = Double.parseDouble(token); // Parse as number
                result = applyOperator(result, value, operator);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid token in formula: " + token);
            }
        }
        return result;
    }

    /**
     * Evaluates a basic arithmetic expression (supports +, -, *, /).
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
                result = applyOperator(result, value, operator);
            }
        }
        return result;
    }

    /**
     * Applies an operator to the current value and a new value.
     * @param current The current value.
     * @param value The new value.
     * @param operator The operator to apply (+, -, *, /).
     * @return The result of applying the operator.
     */
    private double applyOperator(double current, double value, String operator) {
        switch (operator) {
            case "+": return current + value;
            case "-": return current - value;
            case "*": return current * value;
            case "/": return current / value;
            default: return current;
        }
    }

    /**
     * Gets the computed value of the cell (after evaluation).
     * @return The computed value, or null if evaluation failed.
     */
    public Double getComputedValue() {
        return computedValue;
    }

    @Override
    public String toString() {
        if (isEvaluated) {
            return computedValue != null ? computedValue.toString() : "Error in evaluation";
        } else {
            return data;
        }
    }
}
