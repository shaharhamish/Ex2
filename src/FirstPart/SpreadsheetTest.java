package assignments.ex2.src;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SpreadsheetTest {

    @Test
    public void testCellIsNumber() {
        Cell numberCell = new Cell("123");
        assertTrue(numberCell.isNumber(), "Cell should detect numbers correctly");

        Cell nonNumberCell = new Cell("abc");
        assertFalse(nonNumberCell.isNumber(), "Cell should detect non-numbers correctly");
    }

    @Test
    public void testCellIsText() {
        Cell textCell = new Cell("Hello");
        assertTrue(textCell.isText(), "Cell should detect plain text correctly");

        Cell formulaCell = new Cell("=A1 + 5");
        assertFalse(formulaCell.isText(), "Cell with formula should not be plain text");

        Cell numberCell = new Cell("123");
        assertFalse(numberCell.isText(), "Cell with numbers should not be plain text");
    }

    @Test
    public void testCellIsFormula() {
        Cell formulaCell = new Cell("=A1 + 5");
        assertTrue(formulaCell.isFormula(), "Cell should detect formulas correctly");

        Cell nonFormulaCell = new Cell("123");
        assertFalse(nonFormulaCell.isFormula(), "Non-formula cell should not be detected as a formula");
    }

    @Test
    public void testComputeFormula() {
        Spreadsheet spreadsheet = new Spreadsheet(3, 3);
        spreadsheet.set(0, 0, "10"); // A1
        spreadsheet.set(1, 0, "20"); // B1
        spreadsheet.set(2, 0, "=A1 + B1"); // C1

        String result = spreadsheet.eval(2, 0); // Evaluate C1
        assertEquals("30.0", result, "Formula evaluation should produce the correct result");
    }

    @Test
    public void testGetCellPosition() {
        Spreadsheet spreadsheet = new Spreadsheet(3, 3);
        int[] position = spreadsheet.getCellPosition("B2");
        assertArrayEquals(new int[]{1, 1}, position, "Cell position conversion should be correct");
    }

    @Test
    public void testSetAndEvalCell() {
        Spreadsheet spreadsheet = new Spreadsheet(3, 3);
        spreadsheet.set(0, 0, "42"); // A1
        spreadsheet.set(1, 0, "Hello"); // B1

        assertEquals("42", spreadsheet.eval(0, 0), "Cell evaluation should return the correct value for numbers");
        assertEquals("Hello", spreadsheet.eval(1, 0), "Cell evaluation should return the correct value for text");
    }

    @Test
    public void testEvalAll() {
        Spreadsheet spreadsheet = new Spreadsheet(2, 2);
        spreadsheet.set(0, 0, "10"); // A1
        spreadsheet.set(1, 0, "20"); // B1
        spreadsheet.set(0, 1, "=A1 + B1"); // A2

        String[][] result = spreadsheet.evalAll();

        assertEquals("10", result[0][0], "Evaluated value of A1 should match");
        assertEquals("20", result[1][0], "Evaluated value of B1 should match");
        assertEquals("30.0", result[0][1], "Evaluated value of A2 (formula) should match");
    }

    @Test
    public void testFormulaWithInvalidReference() {
        Spreadsheet spreadsheet = new Spreadsheet(3, 3);
        spreadsheet.set(0, 0, "10"); // A1
        spreadsheet.set(1, 0, "=A1 + Z9"); // B1 with invalid reference

        assertEquals("ERR", spreadsheet.eval(1, 0), "Invalid reference in formula should result in 'ERR'");
    }

    @Test
    public void testFormulaWithInvalidOperator() {
        Spreadsheet spreadsheet = new Spreadsheet(3, 3);
        spreadsheet.set(0, 0, "=10 # 5"); // A1 with invalid operator

        assertEquals("ERR", spreadsheet.eval(0, 0), "Invalid operator in formula should result in 'ERR'");
    }

    @Test
    public void testSpreadsheetDisplay() {
        Spreadsheet spreadsheet = new Spreadsheet(2, 2);
        spreadsheet.set(0, 0, "1");
        spreadsheet.set(1, 0, "2");
        spreadsheet.set(0, 1, "=A1 + B1");
        spreadsheet.set(1, 1, "Hello");

        spreadsheet.displaySpreadsheet();
        // Manual verification of the displayed output
    }
}
