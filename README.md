## Ex2: Foundations of Object-Oriented and Recursion
### Introduction
This project focuses on building a basic version of a 
**Spreadsheet**, designed to enhance understanding of:
- Object-Oriented Design principles.
- Recursive problem solving.
- Algorithmic thinking for dependencies and cycles.

#### TO begin we have 2 department Cell and Sprreadsheet that connent to each other like that
+-------------------+       +-------------------------+
|     Cell          |       |     Spreadsheet         |
|-------------------|       |-------------------------|
| - value: String   |       | - cells: Cell[][]       |
|-------------------|       |-------------------------|
| + isNumber(text)  |       | + get(x, y): Cell       |
| + isText(text)    |      | + set(x, y, Cell c)     |
| + isForm(text)    |      | + width(): int          |
| + computeForm(text)|      | + height(): int         |
+-------------------+       | + xCell(c): int         |
^                           | + yCell(c): int         |
|                           | + eval(x, y): String    |
|                           | + evalAll(): String[][] |
|                           | + depth(): int[][]      |
+---------------------+-------------------------+
(contains)                  (contains)

