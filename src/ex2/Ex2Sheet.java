package assignments.ex2.src.ex2;

import java.io.*;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell("");
            }
        }
        eval();
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        Cell c = get(x, y);
        return (c != null) ? c.toString() : Ex2Utils.EMPTY_CELL;
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    public Cell get(String cords) {
        if (cords == null || !cords.matches("[A-Za-z]+[0-9]+")) {
            return null;
        }

        int col = cords.charAt(0) - 'A';
        int row = Integer.parseInt(cords.substring(1)) - 1;

        if (isIn(col, row)) {
            return get(col, row);
        }
        return null; // Return null for invalid references
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
        table[x][y] = new SCell(s);
        eval();
    }

    @Override
    public void eval() {
        for (Cell[] row : table) {
            for (Cell cell : row) {
                if (cell instanceof SCell) {
                    ((SCell) cell).evaluate(this);
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
        int w = width();
        int h = height();
        int[][] ans = new int[w][h];

        // Initialize each cell in the ans array to -1
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                ans[i][j] = -1;
            }
        }

        int depth = 0;    // Current depth level
        int count = 0;    // Number of cells that have been computed
        int max = w * h;  // Total number of cells in the table
        boolean flagC = true;

        // Process cells until all are computed or no progress is made
        while (count < max && flagC) {
            flagC = false;

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    // If the cell can be computed at this depth
                    if (canBeComputedNow(x, y, ans)) {
                        ans[x][y] = depth; // Set its depth
                        count++;           // Increment the counter
                        flagC = true;      // Mark that progress was made
                    }
                }
            }

            depth++; // Increment depth for the next level
        }

        return ans;
    }

    private boolean canBeComputedNow(int x, int y, int[][] ans) {
        Cell cell = get(x, y);
        if (cell == null || ans[x][y] != -1) {
            return false; // Already computed or empty cell
        }

        String content = cell.toString();
        if (content.startsWith("=")) {
            String[] tokens = content.substring(1).split("[^A-Za-z0-9]+");
            for (String token : tokens) {
                if (token.matches("[A-Za-z]+[0-9]+")) {
                    int depX = token.charAt(0) - 'A';
                    int depY = Integer.parseInt(token.substring(1)) - 1;

                    if (isIn(depX, depY) && ans[depX][depY] == -1) {
                        return false; // Dependency is not yet computed
                    }
                }
            }
        }

        return true; // All dependencies are computed
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
        Cell cell = get(x, y);
        return (cell != null) ? cell.toString() : null;
    }
}
