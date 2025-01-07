package assignments.ex2.src.ex2;

public class SCell implements Cell {
    private String line;
    private int type;
    private double computedValue;
    private boolean isEvaluated = false;

    public SCell(String s) {
        setData(s);
    }

    @Override
    public void setData(String s) {
        this.line = s.trim();
        if (line.startsWith("=")) {
            this.type = Ex2Utils.FORM;
        } else {
            try {
                Double.parseDouble(line);
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
        return 0;
    }

    @Override
    public void setOrder(int t) {}

    public void evaluate(Ex2Sheet sheet) {
        if (isEvaluated) {
            return;
        }

        if (line.startsWith("=")) {
            computedValue = evaluateFormula(line.substring(1), sheet);
            isEvaluated = true;
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
        String[] tokens = formula.split("(?=[-+*/])|(?<=[-+*/])");
        double result = 0.0;
        String operator = "+";

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            if ("+-*/".contains(token)) {
                operator = token;
            } else {
                double value;
                if (token.matches("[A-Za-z]+[0-9]+")) { // Cell reference
                    int col = token.charAt(0) - 'A';
                    int row = Integer.parseInt(token.substring(1)) - 1;

                    // Validate cell indices before accessing
                    if (sheet.isIn(col, row)) {
                        Cell refCell = sheet.get(col, row);
                        if (refCell instanceof SCell) {
                            ((SCell) refCell).evaluate(sheet);
                            value = ((SCell) refCell).getComputedValue();
                        } else {
                            value = 0;
                        }
                    } else {
                        value = 0; // Default to 0 for invalid references
                    }
                } else {
                    value = Double.parseDouble(token);
                }
                result = applyOperation(result, value, operator);
            }
        }
        return result;
    }

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
                return newValue;
        }
    }

    public double getComputedValue() {
        return computedValue;
    }

    @Override
    public String toString() {
        if (type == Ex2Utils.FORM) {
            return String.valueOf(computedValue);
        }
        return getData();
    }
}
