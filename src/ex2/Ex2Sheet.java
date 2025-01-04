package assignments.ex2.src.ex2;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Ex2Sheet represents a spreadsheet containing cells that can hold numbers, text, or formulas.
 * It implements the Sheet interface and provides methods for interacting with and evaluating cells.
 */
public class Ex2Sheet implements Sheet {
    private Cell[][] table; // 2D array of cells representing the spreadsheet

    /**
     * Constructor to initialize the spreadsheet with given dimensions.
     * Each cell is initialized as an empty SCell.
     *
     * @param x Width of the spreadsheet (number of columns).
     * @param y Height of the spreadsheet (number of rows).
     */
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell("");  // Default to empty cells
            }
        }
        eval(); // Evaluate all cells to initialize them
    }

    /**
     * Default constructor initializes the spreadsheet with predefined dimensions.
     */
    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    /**
     * Retrieves the value of a cell as a string.
     *
     * @param x X-coordinate of the cell.
     * @param y Y-coordinate of the cell.
     * @return The value of the cell, or an empty cell representation if null.
     */
    @Override
    public String value(int x, int y) {
        Cell cell = get(x, y);
        return cell != null ? cell.toString() : Ex2Utils.EMPTY_CELL;
    }

    /**
     * Retrieves the cell at the specified coordinates.
     *
     * @param x X-coordinate of the cell.
     * @param y Y-coordinate of the cell.
     * @return The cell at the specified coordinates.
     */
    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    /**
     * Retrieves the cell using string-based coordinates (e.g., "A1").
     *
     * @param cords String representation of the cell coordinates.
     * @return The cell at the given coordinates, or null if invalid.
     */
    @Override
    public Cell get(String cords) {
        // Extract column (letter part) and row (number part) from coordinates
        String columnPart = cords.replaceAll("[^A-Za-z]", "");
        String rowPart = cords.replaceAll("[^0-9]", "");

        // Convert column letters to zero-based index
        int x = 0;
        for (int i = 0; i < columnPart.length(); i++) {
            x = x * 26 + (columnPart.charAt(i) - 'A' + 1);
        }
        x--; // Convert to zero-based index

        // Convert row numbers to zero-based index
        int y = Integer.parseInt(rowPart) - 1;

        // Return the cell at the calculated indices
        return get(x, y);
    }

    /**
     * Sets the content of a cell at the specified coordinates.
     *
     * @param x X-coordinate of the cell.
     * @param y Y-coordinate of the cell.
     * @param s Content to set in the cell.
     */
    @Override
    public void set(int x, int y, String s) {
        table[x][y] = new SCell(s);
    }

    /**
     * Evaluates all cells in the spreadsheet to compute their values.
     */
    @Override
    public void eval() {
        Set<String> evaluationStack = new HashSet<>(); // Tracks cells being evaluated to detect cycles
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                SCell cell = (SCell) get(i, j);
                try {
                    cell.evaluate(this, evaluationStack);
                } catch (IllegalArgumentException e) {
                    cell.setType(Ex2Utils.ERR_CYCLE_FORM); // Mark cell with cycle error
                }
            }
        }
    }

    /**
     * Checks if the given coordinates are within the bounds of the spreadsheet.
     *
     * @param x X-coordinate to check.
     * @param y Y-coordinate to check.
     * @return True if the coordinates are valid, false otherwise.
     */
    @Override
    public boolean isIn(int x, int y) {
        return x >= 0 && y >= 0 && x < width() && y < height();
    }

    /**
     * Computes the dependency depth of all cells in the spreadsheet.
     *
     * @return A 2D array representing the dependency depth of each cell.
     */
    @Override
    public int[][] depth() {
        int[][] depths = new int[width()][height()];
        Set<String> evaluationStack = new HashSet<>();
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                SCell cell = (SCell) get(i, j);
                try {
                    depths[i][j] = calculateDepth(cell, evaluationStack);
                } catch (IllegalArgumentException e) {
                    depths[i][j] = -1; // Cycle detected
                }
            }
        }
        return depths;
    }

    /**
     * Recursively calculates the depth of a cell based on its dependencies.
     *
     * @param cell Cell to calculate depth for.
     * @param evaluationStack Set tracking cells being evaluated to detect cycles.
     * @return The dependency depth of the cell.
     */
    private int calculateDepth(SCell cell, Set<String> evaluationStack) {
        if (cell.getOrder() > 0) {
            return cell.getOrder();
        }

        evaluationStack.add(cell.getData());
        int maxDepth = 0;

        // Handle formula dependencies
        if (cell.getData().startsWith("=")) {
            String formula = cell.getData().substring(1);
            String[] parts = formula.split(" ");
            for (String part : parts) {
                if (part.matches("[A-Za-z]+[0-9]+")) {
                    Cell refCell = get(part);
                    if (refCell != null && refCell instanceof SCell) {
                        maxDepth = Math.max(maxDepth, calculateDepth((SCell) refCell, evaluationStack));
                    }
                }
            }
        }

        int newDepth = maxDepth + 1;
        cell.setOrder(newDepth);
        evaluationStack.remove(cell.getData());
        return newDepth;
    }

    /**
     * Evaluates the value of a single cell.
     *
     * @param x X-coordinate of the cell.
     * @param y Y-coordinate of the cell.
     * @return The evaluated value as a string.
     */
    @Override
    public String eval(int x, int y) {
        // Retrieve the cell at the specified coordinates
        SCell sCell = (SCell) get(x, y);

        if (sCell == null) {
            return ""; // Return an empty string if the cell is null
        }

        String data = sCell.getData();

        // Check if the cell contains a formula
        if (data.startsWith("=")) {
            try {
                String formula = data.substring(1); // Remove the '=' prefix
                Set<String> evaluationStack = new HashSet<>(); // Track visited cells for cycle detection
                sCell.evaluate(this, evaluationStack); // Evaluate the formula recursively
                return sCell.getComputedValue() != null ? String.valueOf(sCell.getComputedValue()) : "ERR"; // Return the computed value or error
            } catch (Exception e) {
                return "ERR"; // Return a generic error message for invalid formula
            }
        }

        // Check if the cell contains a number
        try {
            Double.parseDouble(data); // Attempt to parse as a number
            return data; // Return the number as a string if parsing succeeds
        } catch (NumberFormatException e) {
            // If parsing fails, treat the data as text
            return data;
        }
    }


    /**
     * Returns the width (number of columns) of the spreadsheet.
     */
    @Override
    public int width() {
        return table.length;
    }

    /**
     * Returns the height (number of rows) of the spreadsheet.
     */
    @Override
    public int height() {
        return table[0].length;
    }

    /**
     * Loads the spreadsheet content from a file.
     * The file format is expected to have each row as a line, with cell contents separated by commas.
     * Example:
     * A1,A2,A3
     * B1,B2,B3
     *
     * @param fileName The name of the file to load.
     * @throws IOException If an error occurs during file loading.
     */
    @Override
    public void load(String fileName) throws IOException {
        // Open the file using a BufferedReader
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        // Temporary variables to calculate dimensions and store data
        int rows = 0;
        int cols = 0;
        String line;

        // First pass: Calculate the number of rows and columns
        while ((line = reader.readLine()) != null) {
            rows++;
            int currentCols = line.split(",").length;
            if (currentCols > cols) {
                cols = currentCols;
            }
        }
        reader.close();

        // Resize the table based on the calculated dimensions
        table = new SCell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                table[i][j] = new SCell(""); // Initialize cells to empty
            }
        }

        // Second pass: Populate the table with data from the file
        reader = new BufferedReader(new FileReader(fileName));
        int row = 0;
        while ((line = reader.readLine()) != null) {
            String[] cells = line.split(",");
            for (int col = 0; col < cells.length; col++) {
                table[row][col] = new SCell(cells[col]); // Set cell content
            }
            row++;
        }
        reader.close();

        // Evaluate the entire sheet after loading
        eval();
    }


    /**
     * Saves the spreadsheet content to a file.
     * The file will contain each row of the spreadsheet on a new line,
     * with cell contents separated by commas.
     *
     * @param fileName The name of the file to save.
     * @throws IOException If an error occurs during file saving.
     */
    @Override
    public void save(String fileName) throws IOException {
        // Create a BufferedWriter to write to the file
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

        // Iterate through the table and write each cell's data to the file
        for (int i = 0; i < height(); i++) {
            for (int j = 0; j < width(); j++) {
                writer.write(get(i, j).toString()); // Write cell data
                if (j < width() - 1) {
                    writer.write(","); // Add a comma except for the last cell in the row
                }
            }
            writer.newLine(); // Move to the next line after each row
        }

        // Close the writer to ensure the file is saved
        writer.close();
    }

}
