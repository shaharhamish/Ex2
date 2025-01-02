package assignments.ex2.src.ex2;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell(""); // Initialize empty cells
            }
        }
        eval(); // Evaluate formulas after initializing the sheet
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT); // Default to the width and height from Ex2Utils
    }

    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;
        Cell c = get(x, y);
        if (c != null) {
            ans = c.toString(); // Get the string representation of the cell
        }
        return ans;
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y]; // Return the cell at position (x, y)
    }

    @Override
    public Cell get(String cords) {
        // Extract the column (letter part) and row (number part) from the coordinates string
        String columnPart = cords.replaceAll("[^A-Za-z]", ""); // Extract letters (e.g., "A", "B", etc.)
        String rowPart = cords.replaceAll("[^0-9]", ""); // Extract numbers (e.g., "1", "2", etc.)

        // Convert the column (letter) part to the corresponding index (e.g., "A" -> 0, "B" -> 1, ...)
        int x = 0;
        for (int i = 0; i < columnPart.length(); i++) {
            x = x * 26 + (columnPart.charAt(i) - 'A' + 1);
        }
        x--; // Convert to 0-based index

        // Convert the row part to the corresponding index (e.g., "1" -> 0, "2" -> 1, ...)
        int y = Integer.parseInt(rowPart) - 1;

        // Return the cell at the calculated indices
        return get(x, y);
    }


    @Override
    public int width() {
        return table.length; // Return the number of rows
    }

    @Override
    public int height() {
        return table[0].length; // Return the number of columns
    }

    @Override
    public void set(int x, int y, String s) {
        Cell c = new SCell(s);  // Create a new cell with the given string
        table[x][y] = c; // Update the cell at position (x, y)
    }

    @Override
    public void eval() {
        // Evaluate all formulas and handle dependencies
        int[][] dd = depth();  // Compute dependencies for each cell
    }

    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height(); // Check if within bounds
    }

    @Override
    public int[][] depth() {
        int[][] ans = new int[width()][height()];
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                ans[i][j] = -1; // Initialize depth array
            }
        }
        // Calculate the depth for each cell here (add recursion detection)
        return ans;
    }

    @Override
    public void load(String fileName) throws IOException {
        // Load data from a file (not implemented in this snippet)
    }

    @Override
    public void save(String fileName) throws IOException {
        // Save data to a file (not implemented in this snippet)
    }

    @Override
    public String eval(int x, int y) {
        String ans = null;
        if (get(x, y) != null) {
            ans = get(x, y).toString();  // Get the string value of the cell
        }
        return ans;
    }

    // Method to detect cycles in formulas (recursive dependencies)
    private boolean hasCycle(int x, int y, Set<String> visited) {
        String cellKey = x + "," + y;
        if (visited.contains(cellKey)) {
            return true; // Cycle detected
        }
        visited.add(cellKey);
        // If it's a formula, check its dependencies (this part should handle recursion logic)
        return false;
    }

    // Method to resolve and evaluate formulas
    private String resolveFormula(int x, int y) {
        String formula = get(x, y).getData();
        if (formula.startsWith("=")) {
            // Parse and evaluate the formula
            return evaluateFormula(formula);
        }
        return formula;  // If it's not a formula, return the value as is
    }

    // Dummy method for formula evaluation (should be implemented properly)
    private String evaluateFormula(String formula) {
        return formula;  // Placeholder for actual formula evaluation logic
    }
}
