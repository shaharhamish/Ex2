package assignments.ex2.src.ex2;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Ex2Sheet represents a spreadsheet, managing cells and their evaluations.
 */
public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    // Constructor with dimensions
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell("");
            }
        }
        eval();
    }

    // Default constructor
    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;
        Cell c = get(x, y);
        if (c != null) {
            ans = c.toString();
        }
        return ans;
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    public Cell get(String cords) {
        if (cords == null || !cords.matches("[A-Za-z]+[0-9]+")) {
            return null; // Invalid reference
        }

        // Convert column (e.g., A -> 0, B -> 1) and row (e.g., 1 -> 0)
        int col = cords.charAt(0) - 'A';
        int row = Integer.parseInt(cords.substring(1)) - 1;

        if (isIn(col, row)) {
            return get(col, row);
        }
        return null;
    }

    @Override
    public int width() {
        return table.length;
    }

    @Override
    public int height() {
        return table[0].length;
    }

    @Override
    public void set(int x, int y, String s) {
        Cell c = new SCell(s);
        table[x][y] = c;
        eval();
    }

    @Override
    public void eval() {
        Set<String> evaluationStack = new HashSet<>();

        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {
                if (table[i][j] instanceof SCell) {
                    ((SCell) table[i][j]).evaluate(this, evaluationStack);
                }
            }
        }
    }

    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height();
    }

    @Override
    public int[][] depth() {
        int[][] ans = new int[width()][height()];
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                ans[i][j] = 0; // Default depth
            }
        }
        return ans;
    }

    @Override
    public void load(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                String[] cells = line.split(",");
                for (int col = 0; col < cells.length; col++) {
                    set(col, row, cells[col]);
                }
                row++;
            }
        }
    }

    @Override
    public void save(String fileName) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 0; i < height(); i++) {
                for (int j = 0; j < width(); j++) {
                    bw.write(value(j, i));
                    if (j < width() - 1) {
                        bw.write(",");
                    }
                }
                bw.newLine();
            }
        }
    }

    @Override
    public String eval(int x, int y) {
        String ans = null;
        if (get(x, y) != null) {
            ans = get(x, y).toString();
        }
        return ans;
    }
}
