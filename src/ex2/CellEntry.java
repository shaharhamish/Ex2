package assignments.ex2.src.ex2;

public class CellEntry  implements Index2D {
    private String index; // Stores the cell reference
    // Constructor
    public CellEntry(String s)
    {
        index=s;
    }
    public CellEntry(int x,int y)
    {
        char letter = (char) ('A' + x); // ממירים את המספר לאות
        index=String.valueOf(letter)+y;
    }
    @Override
    public int getX() {//Assumes that ASCII code is always converted to uppercase
        if (isValid()) {
            return Character.toUpperCase(index.charAt(0))-65;
        }
        return Ex2Utils.ERR;
    }
    @Override
    public int getY() {
        String copyIndex=index;
        if (isValid()){
            copyIndex=copyIndex.substring(1);
            return Integer.parseInt(copyIndex);
        }
        return Ex2Utils.ERR;
    }
    @Override
    public String toString(){
        if (isValid())
            return index;
        return "";
    }
    @Override
    public boolean isValid(){
        String copyIndex=index;
        if(index==null||index.isEmpty())// Check if the reference is null or empty
            return false;
        if (!(Character.isLetter(index.charAt(0))))// The first character must be a letter
            return false;
        copyIndex=copyIndex.substring(1); // Remove the column
        if(!(copyIndex.length()==1||copyIndex.length()==2))// The remaining part must be 1 or 2 digits
            return false;
        for(int i=0;i<copyIndex.length();i++){
            if (!(Character.isDigit(copyIndex.charAt(i))))// Each character in the remaining part must be a digit
                return false;
        }
        return true;// If all checks pass, the reference is valid
    }
}