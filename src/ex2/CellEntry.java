package assignments.ex2.src.ex2;

/**
 * Represents an entry in a spreadsheet cell with associated data and its position (x, y).
 * Implements the Index2D interface for working with 2D indices.
 */
public class CellEntry implements Index2D {
    private String data; // The content of the cell (formula, number, or text)
    private int x, y;    // The coordinates of the cell

    /**
     * Constructs a CellEntry with given data and coordinates.
     *
     * @param data The content of the cell.
     * @param x    The x-coordinate of the cell.
     * @param y    The y-coordinate of the cell.
     */
    public CellEntry(String data, int x, int y) {
        this.data = data;
        this.x = x;
        this.y = y;
    }

    /**
     * Checks if the cell is valid.
     * A cell is considered valid if its data is not empty.
     *
     * @return true if the cell has non-empty data, false otherwise.
     */
    @Override
    public boolean isValid() {
        return !data.isEmpty();
    }

    /**
     * Returns the x-coordinate of the cell.
     *
     * @return The x-coordinate.
     */
    @Override
    public int getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the cell.
     *
     * @return The y-coordinate.
     */
    @Override
    public int getY() {
        return y;
    }

    /**
     * Returns the data stored in the cell.
     *
     * @return The cell's content (formula, number, or text).
     */
    public String getData() {
        return data;
    }

    /**
     * Updates the data of the cell.
     *
     * @param data The new content to be stored in the cell.
     */
    public void setData(String data) {
        this.data = data;
    }
}
