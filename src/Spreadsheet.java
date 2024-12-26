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
}
