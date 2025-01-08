package assignments.ex2.src.ex2;

/**
 * CellEntry represents a 2D coordinate in a spreadsheet.
 */
public class CellEntry implements Index2D {
    private int x;
    private int y;

    /**
     * Constructor to create a CellEntry with the given coordinates.
     *
     * @param x The x-coordinate (column index).
     * @param y The y-coordinate (row index).
     */
    public CellEntry(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Converts the CellEntry to a human-readable string (e.g., "A1").
     *
     * @return The string representation of the cell.
     */
    @Override
    public String toString() {
        return convertToCellNotation(x, y);
    }

    /**
     * Checks if the CellEntry is valid (non-negative coordinates).
     *
     * @return True if valid; otherwise, false.
     */
    @Override
    public boolean isValid() {
        return x >= 0 && y >= 0;
    }

    /**
     * Gets the x-coordinate.
     *
     * @return The x-coordinate.
     */
    @Override
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate.
     *
     * @return The y-coordinate.
     */
    @Override
    public int getY() {
        return y;
    }

    /**
     * Parses a cell notation (e.g., "A1") into CellEntry coordinates.
     *
     * @param cellNotation The string representation of the cell.
     * @return The CellEntry with parsed coordinates.
     * @throws IllegalArgumentException If the cell notation is invalid.
     */
    public static CellEntry fromString(String cellNotation) {
        if (cellNotation == null || !cellNotation.matches("[A-Za-z]+[0-9]+")) {
            throw new IllegalArgumentException("Invalid cell notation: " + cellNotation);
        }

        String columnPart = cellNotation.replaceAll("[0-9]", "").toUpperCase();
        String rowPart = cellNotation.replaceAll("[A-Za-z]", "");

        int column = convertColumn(columnPart);
        int row = Integer.parseInt(rowPart) - 1;

        return new CellEntry(column, row);
    }

    /**
     * Converts column letters (e.g., "A", "Z", "AA") into a zero-based column index.
     *
     * @param columnLetters The column letters.
     * @return The zero-based column index.
     */
    private static int convertColumn(String columnLetters) {
        int column = 0;
        for (char c : columnLetters.toCharArray()) {
            column = column * 26 + (c - 'A' + 1);
        }
        return column - 1; // Zero-based index
    }

    /**
     * Converts zero-based coordinates into cell notation (e.g., (0, 0) -> "A1").
     *
     * @param x The zero-based column index.
     * @param y The zero-based row index.
     * @return The string representation of the cell.
     */
    private static String convertToCellNotation(int x, int y) {
        StringBuilder column = new StringBuilder();

        int columnIndex = x;
        while (columnIndex >= 0) {
            column.insert(0, (char) ('A' + (columnIndex % 26)));
            columnIndex = (columnIndex / 26) - 1;
        }

        // Convert the zero-based row index to one-based for cell notation
        int rowIndex = y + 1;

        return column.toString() + rowIndex;
    }
}
