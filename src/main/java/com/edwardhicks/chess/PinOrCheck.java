package com.edwardhicks.chess;


public record PinOrCheck(Square pos, int dirCol, int dirRow) {}