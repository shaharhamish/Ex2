package assignments.ex2.src.SecondPart;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class Ex2SheetTest {

    @Test
    void testSheetInitialization() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        assertEquals(5, sheet.width());
        assertEquals(5, sheet.height());

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                assertEquals("", sheet.value(x, y));
            }
        }
    }

    @Test
    void testDefaultConstructor() {
        Ex2Sheet sheet = new Ex2Sheet();
        assertEquals(Ex2Utils.WIDTH, sheet.width());
        assertEquals(Ex2Utils.HEIGHT, sheet.height());
    }

    @Test
    void testSetAndGetValue() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "123");
        assertEquals("123", sheet.value(0, 0));
    }

    @Test
    void testSetFormulaAndEvaluate() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "2");
        sheet.set(1, 0, "3");
        sheet.set(2, 0, "=A1+B1");
        assertEquals("5.0", sheet.value(2, 0));
    }

    @Test
    void testInvalidFormula() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=A1+");
        assertEquals(Ex2Utils.ERR_FORM_FORMAT, sheet.get(0, 0).getType());
    }

    @Test
    void testCircularReference() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=B1");
        sheet.set(1, 0, "=A1");
        assertEquals(Ex2Utils.ERR_CYCLE_FORM, sheet.get(0, 0).getType());
        assertEquals(Ex2Utils.ERR_CYCLE_FORM, sheet.get(1, 0).getType());
    }

    @Test
    void testLoadFromFile() throws IOException {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        String testFile = "test.csv";
        File file = new File(testFile);

        try {
            // Write test data to a file
            sheet.set(0, 0, "123");
            sheet.set(1, 1, "=A1");
            sheet.save(testFile);

            // Load the data back into a new sheet
            Ex2Sheet loadedSheet = new Ex2Sheet();
            loadedSheet.load(testFile);

            assertEquals("123", loadedSheet.value(0, 0));
            assertEquals("123.0", loadedSheet.value(1, 1));
        } finally {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Test
    void testSaveToFile() throws IOException {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "123");
        sheet.set(1, 1, "=A1");

        String testFile = "test.csv";
        File file = new File(testFile);

        try {
            sheet.save(testFile);
            assertTrue(file.exists());
        } finally {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Test
    void testInvalidCoordinates() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        assertNull(sheet.get(-1, -1));
        assertNull(sheet.get(10, 10));
        assertEquals(Ex2Utils.EMPTY_CELL, sheet.value(10, 10));
    }
}