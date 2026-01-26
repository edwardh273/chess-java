package com.edwardhicks.chess;

public class CastleRights {
    public boolean wks;
    public boolean bks;
    public boolean wqs;
    public boolean bqs;

    public CastleRights(boolean wks, boolean bks, boolean wqs, boolean bqs) {
        this.wks = wks;
        this.bks = bks;
        this.wqs = wqs;
        this.bqs = bqs;
    }
}