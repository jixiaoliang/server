package com.chanchan.game.match;

public class UnitCost {
  public static int estimate(String unitId) {
    if (unitId == null || unitId.length() < 3) return 1;
    int num;
    try {
      num = Integer.parseInt(unitId.substring(2));
    } catch (NumberFormatException ex) {
      return 1;
    }
    if (num <= 5) return 1;
    if (num <= 10) return 2;
    if (num <= 15) return 3;
    if (num <= 20) return 4;
    if (num <= 25) return 5;
    return 3;
  }
}
