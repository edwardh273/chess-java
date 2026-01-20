package com.edwardhicks.chess;


public class Square {
    private int col;
    private int row;

    public Square(int col, int row) {
        this.col = col;
        this.row = row;
    }

    // Overriding the toString() method
    @Override
    public String toString() {
        return "Col: " + this.getCol() + " Row: " + this.getRow();
    }

    // Getters, so other classes can access these values
    public int getCol() { return col; }
    public int getRow() { return row; }
}
