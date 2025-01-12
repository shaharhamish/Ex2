package assignments.ex2.src.ex2;

/**
 * The CellEntry class represents a cell in a spreadsheet-like structure.
 * It implements the Index2D interface, allowing access to a cell's 
 * row (X-coordinate) and column (Y-coordinate) using a human-readable 
 * string index (e.g., "A1").
 *
 * <p>This class provides utilities to:
 * - Parse a string index into row and column values.
 * - Validate the format of the cell reference.
 * - Construct a cell reference from numeric row and column indices.</p>
 *
 * <h2>Examples</h2>
 * <ul>
 *   <li>Construct a cell using a string: <code>CellEntry cell = new CellEntry("A1");</code></li>
 *   <li>Construct a cell using numeric indices: <code>CellEntry cell = new CellEntry(0, 1);</code></li>
 *   <li>Retrieve the X-coordinate: <code>int x = cell.getX();</code></li>
 *   <li>Retrieve the Y-coordinate: <code>int y = cell.getY();</code></li>
 *   <li>Check if the cell reference is valid: <code>boolean valid = cell.isValid();</code></li>
 * </ul>
 */
public class CellEntry implements Index2D {

    // Stores the cell reference as a string, e.g., "A1".
    private String  index;

    /**
     * Constructs a CellEntry object using a string cell reference.
     *
     * @param s the cell reference in the format of a letter followed by a number (e.g., "A1").
     */
    public CellEntry(String s) {
        index = s;
    }

    /**
     * Constructs a CellEntry object using numeric row and column indices.
     * The row (x) is converted to a letter starting from 'A', 
     * and the column (y) remains numeric.
     *
     * @param x the row index (0-based).
     * @param y the column index (numeric).
     */
    public CellEntry(int x, int y) {
        char letter = (char) ('A' + x); // Convert the row number to a letter.
        index = String.valueOf(letter) + y;
    }

    /**
     * Retrieves the row index (X-coordinate) of the cell.
     *
     * @return the row index as a 0-based integer, or {@code Ex2Utils.ERR} if invalid.
     */
    @Override
    public int getX() {
        if (isValid()) {
            // Convert the first character to uppercase and map it to a 0-based index.
            return Character.toUpperCase(index.charAt(0)) - 65;
        }
        return Ex2Utils.ERR;
    }

    /**
     * Retrieves the column index (Y-coordinate) of the cell.
     *
     * @return the column index as an integer, or {@code Ex2Utils.ERR} if invalid.
     */
    @Override
    public int getY() {
        String copyIndex = index;
        if (isValid()) {
            // Extract and parse the numeric portion of the cell reference.
            copyIndex = copyIndex.substring(1);
            return Integer.parseInt(copyIndex);
        }
        return Ex2Utils.ERR;
    }

    /**
     * Returns the cell reference as a string.
     *
     * @return the cell reference (e.g., "A1") if valid, or an empty string if invalid.
     */
    @Override
    public String toString() {
        if (isValid()) {
            return index;
        }
        return "";
    }

    /**
     * Validates the cell reference format.
     * A valid reference consists of:
     * - A single uppercase or lowercase letter for the row.
     * - A numeric value (1 or 2 digits) for the column.
     *
     * @return {@code true} if the reference is valid; {@code false} otherwise.
     */
    @Override
    public boolean isValid() {
        String copyIndex = index;
        // Check if the reference is null or empty.
        if (index == null || index.isEmpty()) {
            return false;
        }
        // Ensure the first character is a letter.
        if (!Character.isLetter(index.charAt(0))) {
            return false;
        }
        // Remove the row letter and validate the column part.
        copyIndex = copyIndex.substring(1);
        // Column must be 1 or 2 digits long.
        if (!(copyIndex.length() == 1 || copyIndex.length() == 2)) {
            return false;
        }
        // Ensure all remaining characters are digits.
        for (int i = 0; i < copyIndex.length(); i++) {
            if (!Character.isDigit(copyIndex.charAt(i))) {
                return false;
            }
        }
        return true; // Reference is valid.
    }
}
