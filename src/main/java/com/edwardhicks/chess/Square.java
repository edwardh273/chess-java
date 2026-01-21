package com.edwardhicks.chess;


public record Square(int col, int row) {

    // Overriding the toString() method
    @Override
    public String toString() {
        return "Col: " + this.col() + " Row: " + this.row();
    }
}
