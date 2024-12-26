import java.util.*;

public class Cell {
    private String value;

    // Constructor: Initializes the cell's content.
    public Cell(String value) {
        this.value = value;
    }

    // Determines if the given text is a valid number (integer or double).
    public boolean isNumber(String text) {
        boolean ans;
        try {
            Double.parseDouble(text);
            ans = true;
        } catch (NumberFormatException e) {
            ans = false;
        }
        return ans;
    }

    // Determines if the given text is valid as plain text (neither a number nor a formula).
    public boolean isText(String text) {
        return !isNumber(text) && !isForm(text);
    }

    // Checks if the text represents a valid formula starting with '='.
    public boolean isForm(String text) {
        if (!text.startsWith("=")) return false;
        String formula = text.substring(1);
        try {
            computeForm(formula);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Computes the value of a formula, assuming it is valid.
    public Double computeForm(String form) {
        try {
            // A very basic implementation using the built-in JavaScript engine.
            // In a real implementation, use a proper formula parser.
            form = form.replaceAll("\\(([^()]+)\\)", "($1)"); // Ensure parentheses are valid
            return eval(form);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid formula");
        }
    }

    // Evaluates a mathematical expression using stacks for numbers and operators.
    private Double eval(String expression) {
        // Simple evaluation for the sake of demonstration (can be replaced with a parser)
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    sb.append(expression.charAt(i));
                    i++;
                }
                i--;
                numbers.push(Double.parseDouble(sb.toString()));
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(c)) {
                    numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.push(c);
            }
        }

        while (!operators.isEmpty()) {
            numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
        }

        return numbers.pop();
    }

    // Determines the precedence of an operator. Higher value indicates higher precedence.
    private int precedence(char op) {
        return (op == '+' || op == '-') ? 1 : (op == '*' || op == '/') ? 2 : 0;
    }

    // Applies an operator to two operands and returns the result.
    private double applyOperator(char op, double b, double a) {
        return switch (op) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> a / b;
            default -> 0;
        };
    }

    // Getter for the value of the cell.
    public String getValue() {
        return value;
    }

    //Setter for the value of the cell.
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Cell{" + "value='" + value + '\'' + '}';
    }
}
