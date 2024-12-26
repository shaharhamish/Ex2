public class Spreadsheet {
    private Cell[][] cells;
    //constructor
    public Spreadsheet(int width, int height) {
        cells = new Cell[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                cells[i][j] = new Cell("");
            }
        }
    }
    //Set function for spreadsheet
    public void set(int x, int y, Cell cell) {
        cells[x][y] = cell;
    }
    //Get function for spreadsheet
    public Cell get(int x, int y) {
        return cells[x][y];
    }
    //get width
    public int width() {
        return cells.length;
    }
    //get height
    public int height() {
        return cells[0].length;
    }
}
