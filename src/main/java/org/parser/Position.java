package org.parser;

/**
 * A class defining a point in the input.
 */
public class Position {
    public int line = 1;
    public int column = 1;

    public Position() {
        line = 1;
        column = 1;
    }

    public Position(int l, int t) {
        line = l;
        column = t;
    }

    public Position(Position p) {
        line = p.line;
        column = p.column;
    }

    public void set(Position p) {
        line = p.line;
        column = p.column;
    }

    public boolean equals(Position l) {
        return l.line == line && l.column == column;
    }

    public String toString() {
        return Integer.toString(line) + "." + Integer.toString(column);
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }
}
