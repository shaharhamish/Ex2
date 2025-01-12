package assignments.ex2.src;

public class Spreadsheet {
    private final Cell[][] cells;

    // Constructor: Initializes the spreadsheet with the given dimensions and empty cells.
    public Spreadsheet(int width, int height) {
        cells = new Cell[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                cells[i][j] = new Cell("");
            }
        }
    }

    // Sets the value of a specific cell.
    public void set(int x, int y, String value) {
        cells[x][y].setValue(value);
    }

    // Retrieves the cell object at the given position.
    public Cell getCell(int x, int y) {
        return cells[x][y];
    }

    // Retrieves the width (number of columns) of the spreadsheet.
    public int width() {
        return cells.length;
    }

    // Retrieves the height (number of rows) of the spreadsheet.
    public int height() {
        return cells[0].length;
    }

    // Converts a cell reference (e.g., "A1") to x and y positions in the grid.
    public int[] getCellPosition(String reference) {
        char column = reference.charAt(0);
        int row = Integer.parseInt(reference.substring(1)) - 1;
        return new int[]{column - 'A', row};
    }

    // Evaluates the content of a specific cell.
    public String eval(int x, int y) {
        Cell cell = getCell(x, y);
        String value = cell.getValue();

        if (cell.isNumber()) {
            return value;
        } else if (cell.isText()) {
            return value;
        } else if (cell.isFormula()) {
            try {
                String formula = value.substring(1); // Remove '='
                double result = cell.computeFormula(formula, this);
                return String.valueOf(result);
            } catch (Exception e) {
                return "ERR";
            }
        }
        return "ERR";
    }

    // Evaluates all cells in the spreadsheet and returns their values as a 2D array.
    public String[][] evalAll() {
        int w = width();
        int h = height();
        String[][] result = new String[w][h];

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                result[i][j] = eval(i, j);
            }
        }
        return result;
    }

    // Displays the spreadsheet in a readable format.
    public void displaySpreadsheet() {
        int w = width();
        int h = height();

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                System.out.print(eval(i, j) + "\t");
            }
            System.out.println();
        }
    }
}
