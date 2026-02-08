package com.edwardhicks.chess;

import java.util.List;

public record PinsAndChecks(boolean inCheck, List<PinOrCheck> pins, List<PinOrCheck> checks) {}