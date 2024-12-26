public class Spreadsheet {
    private Cell[][] cells;
    // Constructor: Initializes the spreadsheet with the given dimensions and empty cells
    public Spreadsheet(int width, int height) {
        cells = new Cell[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                cells[i][j] = new Cell("");
            }
        }
    }
    //Sets the content of a specific cell
    public void set(int x, int y, Cell cell) {
        cells[x][y] = cell;
    }
    // Retrieves the content of a specific cell
    public Cell get(int x, int y) {
        return cells[x][y];
    }
    // Returns the width (number of columns) of the spreadsheet.
    public int width() {
        return cells.length;
    }
    // Returns the height (number of rows) of the spreadsheet.
    public int height() {
        return cells[0].length;
    }
    // Evaluates the content of a specific cell.
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        if (cell.isNumber(cell.getValue())) {
            return cell.getValue();
        } else if (cell.isText(cell.getValue())) {
            return cell.getValue();
        } else if (cell.isForm(cell.getValue())) {
            try {
                return String.valueOf(cell.computeForm(cell.getValue().substring(1)));
            } catch (Exception e) {
                return "ERR_FORM";
            }
        }
        return "ERR";
    }

    // Evaluates all cells in the spreadsheet and returns their values as a 2D array.
    public String[][] evalAll() {
        int w = width(), h = height();
        String[][] result = new String[w][h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                result[i][j] = eval(i, j); // Evaluate each cell.
            }
        }
        return result;
    }

    // Calculates the computational depth of each cell (not implemented yet).
    public int[][] depth() {
        return new int[width()][height()];
    }
}
