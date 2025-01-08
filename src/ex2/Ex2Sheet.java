// Updated Ex2Sheet.java
package assignments.ex2.src.ex2;

import java.io.*;

public class Ex2Sheet implements Sheet {
    // Two-dimensional array representing the spreadsheet's cells
    private SCell[][] table;

    // Constructor to initialize the spreadsheet with specified dimensions
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell(""); // Initialize each cell with an empty value
            }
        }
        eval(); // Evaluate the entire spreadsheet on creation
    }

    // Default constructor initializing with predefined dimensions
    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    // Retrieve the value of a cell as a string
    @Override
    public String value(int x, int y) {
        Cell c = get(x, y);
        return (c != null) ? c.toString() : Ex2Utils.EMPTY_CELL; // Return empty if cell is null
    }

    // Retrieve a cell object given its coordinates
    @Override
    public Cell get(int x, int y) {
        if (isIn(x, y)) {
            return table[x][y];
        }
        return null;
    }

    // Retrieve a cell object given its string-based coordinates (e.g., "A1")
    public SCell get(String cords) {
        if (cords == null || !cords.matches("[A-Za-z]+[0-9]+")) {
            return null;
        }

        int col = 0;
        String letters = cords.replaceAll("[0-9]", "");
        for (int i = 0; i < letters.length(); i++) {
            col = col * 26 + (letters.charAt(i) - 'A' + 1);
        }
        col--; // Convert to zero-based index

        int row = Integer.parseInt(cords.replaceAll("[A-Za-z]", ""));

        if (isIn(col, row)) {
            return (SCell) get(col, row);
        }
        return null;
    }

    // Update a cell's data and trigger spreadsheet evaluation
    @Override
    public void set(int x, int y, String s) {
        if (!isIn(x, y)) return; // Ensure coordinates are within bounds

        SCell cell = table[x][y];
        if (cell == null) {
            cell = new SCell(s);
            table[x][y] = cell; // Assign a new cell if it doesn't exist
        } else {
            cell.setData(s); // Update the existing cell's data
        }

        // Evaluate the current cell and propagate changes to dependent cells
        cell.evaluate(this);
        propagateDependencies(cell);
    }

    // Propagate changes to cells that depend on the updated cell
    private void propagateDependencies(SCell updatedCell) {
        for (int x = 0; x < table.length; x++) {
            for (int y = 0; y < table[0].length; y++) {
                SCell cell = table[x][y];
                if (cell != null && cell.hasDependencyOn(updatedCell)) {
                    cell.evaluate(this); // Re-evaluate dependent cells
                }
            }
        }
    }

    // Evaluate all cells in the spreadsheet
    @Override
    public void eval() {
        for (SCell[] row : table) {
            for (SCell cell : row) {
                if (cell != null) {
                    cell.evaluate(this); // Evaluate each cell
                }
            }
        }
    }

    // Check if the given coordinates are within bounds
    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < table.length && yy < table[0].length;
    }

    // Get the width of the spreadsheet
    @Override
    public int width() {
        return table.length;
    }

    // Get the height of the spreadsheet
    @Override
    public int height() {
        return table[0].length;
    }

    // Determine the computation depth of each cell in the spreadsheet
    @Override
    public int[][] depth() {
        int w = width();
        int h = height();
        int[][] ans = new int[w][h];

        // Initialize all depths to -1 (uncomputed)
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                ans[i][j] = -1;
            }
        }

        int depth = 0;
        int count = 0;
        int max = w * h;
        boolean flagC = true;

        while (count < max && flagC) {
            flagC = false;

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    if (canBeComputedNow(x, y, ans)) {
                        ans[x][y] = depth; // Set depth for the cell
                        count++;
                        flagC = true;
                    }
                }
            }
            depth++;
        }

        return ans;
    }

    // Check if a cell can be computed at the current depth
    private boolean canBeComputedNow(int x, int y, int[][] ans) {
        SCell cell = table[x][y];
        if (cell == null || ans[x][y] != -1) {
            return false; // Skip cells already computed or null cells
        }

        String content = cell.getData();
        if (content.startsWith("=")) {
            String formula = content.substring(1); // Remove '=' from the formula
            int i = 0;
            while (i < formula.length()) {
                char currentChar = formula.charAt(i);

                if (Character.isLetter(currentChar)) {
                    // Extract cell reference
                    StringBuilder ref = new StringBuilder();
                    while (i < formula.length() && Character.isLetter(formula.charAt(i))) {
                        ref.append(formula.charAt(i));
                        i++;
                    }

                    int col = convertColumnToIndex(ref.toString());
                    StringBuilder num = new StringBuilder();
                    while (i < formula.length() && Character.isDigit(formula.charAt(i))) {
                        num.append(formula.charAt(i));
                        i++;
                    }
                    int row = Integer.parseInt(num.toString());

                    if (isIn(col, row) && ans[col][row] == -1) {
                        return false; // Dependency not yet computed
                    }
                } else if ("+-*/".indexOf(currentChar) >= 0) {
                    i++; // Skip operators
                } else if (currentChar == '(' || currentChar == ')') {
                    i++; // Skip parentheses
                } else {
                    i++; // Skip other characters
                }
            }
        }
        return true;
    }

    // Convert a column label (e.g., "A") to its zero-based index
    private int convertColumnToIndex(String column) {
        int index = 0;
        for (int i = 0; i < column.length(); i++) {
            index = index * 26 + (column.charAt(i) - 'A' + 1);
        }
        return index - 1;
    }

    // Load spreadsheet data from a file
    @Override
    public void load(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split each line into components by comma
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Invalid file format. Expected lines in the format 'x,y,value'.");
                }

                try {
                    // Parse coordinates and value
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());
                    String value = parts[2].trim();

                    // Set the value in the spreadsheet
                    set(x, y, value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid file format. Coordinates must be integers.", e);
                }
            }
        }
    }

    // Save spreadsheet data to a file
    @Override
    public void save(String fileName) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (int x = 0; x < width(); x++) {
                for (int y = 0; y < height(); y++) {
                    String value = table[x][y].getData().trim(); // Get the cell's raw data
                    if (!value.isEmpty()) { // Only save cells with non-empty values
                        bw.write(x + "," + y + "," + value);
                        bw.newLine();
                    }
                }
            }
        }
    }


    // Evaluate a specific cell and return its value
    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        return (cell != null) ? cell.toString() : null;
    }

    // Print the entire spreadsheet to the console
    public void printSheet() {
        for (int y = 0; y < table[0].length; y++) {
            for (int x = 0; x < table.length; x++) {
                System.out.print(value(x, y) + "\t"); // Tab-separated values
            }
            System.out.println();
        }
    }
}