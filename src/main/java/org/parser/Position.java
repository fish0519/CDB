package org.parser;

public class Position {

    public int line;
    public int column;
    public long chars;

    public Position(int line, int column, long chars) {
        this.line = line;
        this.column = column;
        this.chars = chars;
    }

    @Override
    public String toString() {
        return "Position{" +
                "line=" + line +
                ", column=" + column +
                ", chars=" + chars +
                '}';
    }
}
