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
        this.isEvaluated = false;
        this.isInCycle = false;
        if (line.startsWith("=")) {
            if (line.length() != 1) {
                this.type = Ex2Utils.FORM;
            } else {
                this.type = Ex2Utils.ERR_FORM_FORMAT;
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
        return 0;
    }

    @Override
    public void setOrder(int t) {
    }

    public void evaluate(Ex2Sheet sheet) {
        // Skip evaluation if the cell is marked as an error due to invalid formula
        if (isEvaluated || type == Ex2Utils.ERR_FORM_FORMAT) {
            return;
        }

        if (isInCycle) {
            this.type = Ex2Utils.ERR_CYCLE_FORM;
            this.computedValue = 0;
            isEvaluated = true;
            return;
        }

        isInCycle = true;

        if (line.startsWith("=")) {
            try {
                computedValue = evaluateFormula(line.substring(1), sheet);
                this.type = Ex2Utils.FORM;
            } catch (ArithmeticException | IllegalArgumentException e) {
                this.type = Ex2Utils.ERR_FORM_FORMAT;
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

    private double evaluateFormula(String formula, Ex2Sheet sheet) {
        formula = formula.replaceAll("\\s", "");
        return evaluateExpression(formula, sheet);
    }

    private double evaluateExpression(String formula, Ex2Sheet sheet) {
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
                while (i < formula.length() && (Character.isDigit(formula.charAt(i)) || Character.isLetter(formula.charAt(i)))) {
                    operand.append(formula.charAt(i++));
                }
                double value = parseOperand(operand.toString(), sheet);

                if (Double.isNaN(value)) {
                    this.type = Ex2Utils.ERR_FORM_FORMAT;
                    throw new IllegalArgumentException("ERR_FORM due to operand error");
                }

                result = applyOperation(result, value, operator);
            }
        }
        return result;
    }

    private double parseOperand(String operand, Ex2Sheet sheet) {
        operand = operand.toUpperCase();

        if (operand.matches("[A-Za-z]+[0-9]+")) {
            int col = convertColumnToIndex(operand.replaceAll("[0-9]", ""));
            int row = Integer.parseInt(operand.replaceAll("[A-Za-z]", ""));

            if (!sheet.isIn(col, row)) {
                throw new IllegalArgumentException("Invalid cell reference: " + operand);
            }

            SCell refCell = (SCell) sheet.get(col, row);
            if (refCell == null || refCell.getData().isEmpty() || refCell.getType() == Ex2Utils.ERR_FORM_FORMAT) {
                throw new IllegalArgumentException("ERR_FORM: Referenced cell is invalid or empty");
            }

            refCell.evaluate(sheet);
            addDependentCell(refCell);
            return refCell.getComputedValue();
        } else {
            try {
                return Double.parseDouble(operand);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid token in formula: " + operand);
            }
        }
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
                if (newValue == 0) throw new ArithmeticException("Division by zero");
                return currentValue / newValue;
            default:
                return newValue;
        }
    }

    private int convertColumnToIndex(String column) {
        int index = 0;
        for (int i = 0; i < column.length(); i++) {
            index = index * 26 + (column.charAt(i) - 'A' + 1);
        }
        return index - 1;
    }

    public double getComputedValue() {
        return computedValue;
    }

    @Override
    public String toString() {
        if (type == Ex2Utils.NUMBER) {
            return String.format("%.1f", computedValue);
        }
        if (type == Ex2Utils.FORM) {
            return String.valueOf(computedValue);
        }
        if (type == Ex2Utils.ERR_FORM_FORMAT) {
            return "ERR_FORM";
        }
        if (type == Ex2Utils.ERR_CYCLE_FORM) {
            return "ERR_CYCL";
        }
        return getData();
    }

    public void addDependentCell(SCell dependentCell) {
        if (dependentCount >= dependentCells.length) {
            SCell[] newDependentCells = new SCell[dependentCells.length * 2];
            System.arraycopy(dependentCells, 0, newDependentCells, 0, dependentCells.length);
            dependentCells = newDependentCells;
        }
        dependentCells[dependentCount++] = dependentCell;
    }

    public void setPosition(int col, int row) {
        this.colIndex = col;
        this.rowIndex = row;
    }

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
        if (line.startsWith("=")) {
            String formula = line.substring(1);
            return formula.contains(other.getReference());
        }
        return false;
    }
}
