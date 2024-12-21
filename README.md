# Ex2: Foundations of Object-Oriented and Recursion

## Introduction
This project is part of the Introduction to Computer Science course (2025A) at Ariel University's School of Computer Science. It focuses on building a basic version of a **Spreadsheet**, designed to enhance understanding of:

- Object-Oriented Design principles.
- Recursive problem solving.
- Algorithmic thinking for dependencies and cycles.

## Features
The spreadsheet is a 2D array of "Cells," each of which can contain:

- **Text** (e.g., "Hello", "ABC").
- **Numbers** (e.g., `1`, `-2.3`, `3.14`).
- **Formulas**: These can include operations and references to other cells.

### Examples of Valid Formulas:
- `=1`, `=1+2*3`, `=(2+3)/A1`

### Invalid Formulas:
- Missing operands: `=1+`
- Syntax errors: `(3+1*)`
- Cyclic dependencies: `A1 -> A2 -> A1`

## Getting Started
### Prerequisites
- Java Development Kit (JDK) 17 or later.
- IntelliJ IDEA or a similar Java IDE.
- GitHub account for version control.

### Installation
1. Clone this repository:
   ```bash
   git clone <repository-url>
   ```
2. Open the project in IntelliJ IDEA.
3. Ensure the JDK is correctly configured in the project settings.

## Project Setup
### Phase 1: Design
1. **Run the provided partial solution:**
   Download `Ex2Sol_obf.jar` from Moodle and execute:
   ```bash
   java -jar Ex2Sol_obf.jar
   ```
   Observe its behavior to understand the expected functionality.

2. **Start a new project:**
    - Create a new Java project named `Ex2`.
    - Share the project on GitHub as a private repository.

3. **Plan the implementation:**
    - **Cell Class:** Define methods to handle text, numbers, and formulas.
    - **Spreadsheet Class:** Design a 2D array structure with utility methods for setting and retrieving cells, evaluating formulas, and detecting errors or cycles.

4. **Add a descriptive README:** This document should explain the project and its purpose.

## Example Image
![Spreadsheet Example](https://dummyimage.com/800x400/cccccc/000000&text=Spreadsheet+Example)
## Next Steps
- Complete the design phase by creating detailed UML diagrams (optional).
- Implement basic functionality for `Cell` and `Spreadsheet` classes.
- Write unit tests to validate the implementation.

---
For questions or clarifications, please contact the course TA at: **idokah@ariel.ac.il**.

Happy Coding! 🎉
