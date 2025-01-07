package assignments.ex2.src.ex2;

import java.io.*;

public class Ex2Sheet implements Sheet {
    private SCell[][] table;

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
        if (isIn(x, y)) {
            return table[x][y];
        }
        return null;
    }

    public SCell get(String cords) {
        if (cords == null || !cords.matches("[A-Za-z]+[0-9]+")) {
            return null;
        }

        int col = 0;
        String letters = cords.replaceAll("[0-9]", "");
        for (int i = 0; i < letters.length(); i++) {
            col = col * 26 + (letters.charAt(i) - 'A' + 1);
        }
        col--; // Convert to zero-based index

        int row = Integer.parseInt(cords.replaceAll("[A-Za-z]", ""));

        if (isIn(col, row)) {
            return (SCell) get(col, row);
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
        if (!isIn(x, y)) return;

        SCell cell = table[x][y];
        if (cell == null) {
            cell = new SCell(s);
            table[x][y] = cell;
        } else {
            cell.setData(s);
        }

        eval();
    }

    @Override
    public void eval() {
        for (SCell[] row : table) {
            for (SCell cell : row) {
                if (cell != null) {
                    cell.evaluate(this);
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

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                ans[i][j] = -1;
            }
        }

        int depth = 0;
        int count = 0;
        int max = w * h;
        boolean flagC = true;

        while (count < max && flagC) {
            flagC = false;

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    if (canBeComputedNow(x, y, ans)) {
                        ans[x][y] = depth;
                        count++;
                        flagC = true;
                    }
                }
            }
            depth++;
        }

        return ans;
    }

    private boolean canBeComputedNow(int x, int y, int[][] ans) {
        SCell cell = table[x][y];
        if (cell == null || ans[x][y] != -1) {
            return false;
        }

        String content = cell.getData();
        if (content.startsWith("=")) {
            String formula = content.substring(1); // Remove '=' sign from the formula
            int i = 0;
            while (i < formula.length()) {
                char currentChar = formula.charAt(i);

                if (Character.isLetter(currentChar)) {
                    // Cell reference detected
                    StringBuilder ref = new StringBuilder();
                    while (i < formula.length() && Character.isLetter(formula.charAt(i))) {
                        ref.append(formula.charAt(i));
                        i++;
                    }

                    int col = convertColumnToIndex(ref.toString());
                    StringBuilder num = new StringBuilder();
                    while (i < formula.length() && Character.isDigit(formula.charAt(i))) {
                        num.append(formula.charAt(i));
                        i++;
                    }
                    int row = Integer.parseInt(num.toString());

                    if (isIn(col, row) && ans[col][row] == -1) {
                        return false;
                    }
                } else if ("+-*/".indexOf(currentChar) >= 0) {
                    i++;
                } else if (currentChar == '(') {
                    // Handle opening parenthesis (sub-expression)
                    i++;
                } else if (currentChar == ')') {
                    // Handle closing parenthesis (end of sub-expression)
                    i++;
                } else {
                    i++;
                }
            }
        }
        return true;
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

    private int columnLetterToIndex(String column) {
        int index = 0;
        for (int i = 0; i < column.length(); i++) {
            index = index * 26 + (column.charAt(i) - 'A' + 1);
        }
        return index - 1;
    }

    private int convertColumnToIndex(String column) {
        int index = 0;
        for (int i = 0; i < column.length(); i++) {
            index = index * 26 + (column.charAt(i) - 'A' + 1);
        }
        return index - 1; // Convert to zero-based index
    }
}
