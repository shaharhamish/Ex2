package assignments.ex2.src.ex2;
public class CellEntry implements Index2D {
    private String data;
    private int x, y;

    public CellEntry(String data, int x, int y) {
        this.data = data;
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean isValid() {
        // A cell is considered valid if it is not empty
        return !data.isEmpty();
    }

    @Override
    public int getX() {
        return x; // Return x coordinate
    }

    @Override
    public int getY() {
        return y; // Return y coordinate
    }

    public String getData() {
        return data; // Return the data (either formula, number, or text)
    }

    public void setData(String data) {
        this.data = data; // Update cell data
    }
}
