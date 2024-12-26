public class Cell {
    private String value;

    public Cell(String value) {
        this.value = value;
    }

    public boolean isNumber(String text) {
        boolean ans;
        try {
            Double.parseDouble(text);
            ans = true;
        } catch (NumberFormatException e) {
            ans = false;
        }
        return ans;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Cell{" + "value='" + value + '\'' + '}';
    }
}
