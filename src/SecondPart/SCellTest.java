package assignments.ex2.src.SecondPart;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SCellTest {

    @Test
    void testCellInitialization() {
        SCell cell = new SCell("123");
        assertEquals(Ex2Utils.NUMBER, cell.getType());
        assertEquals(123.0, cell.getComputedValue());
    }

    @Test
    void testFormulaCell() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "2");
        sheet.set(1, 0, "3");
        SCell cell = new SCell("=A1+B1");
        cell.evaluate(sheet);
    }

    @Test
    void testInvalidFormulaCell() {
        SCell cell = new SCell("=A1+");
        assertEquals(Ex2Utils.ERR_FORM_FORMAT, cell.getType());
    }

    @Test
    void testSelfReferenceDetection() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=A1");
        sheet.set(1, 0, "=A0");
        SCell cell = (SCell) sheet.get(0, 0);
        assertEquals(Ex2Utils.ERR_CYCLE_FORM, cell.getType());
    }

    @Test
    void testTextCell() {
        SCell cell = new SCell("Hello");
        assertEquals(Ex2Utils.TEXT, cell.getType());
        assertEquals("Hello", cell.getData());
    }

    @Test
    void testEmptyCell() {
        SCell cell = new SCell("");
        assertEquals(Ex2Utils.TEXT, cell.getType());
        assertEquals("", cell.getData());
    }
}