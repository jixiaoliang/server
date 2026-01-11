package com.chanchan.game.match;

public class EconomyRules {
  public int baseGoldPerRound = 5;
  public int interestCap = 5;
  public int refreshCost = 2;
  public int[] levelCosts = { 0, 2, 6, 10, 20, 36, 56, 80, 100 };

  public int calcInterest(int gold) {
    return Math.min(interestCap, gold / 10);
  }

  public int levelUpCost(int level) {
    if (level < 0 || level >= levelCosts.length) return 0;
    return levelCosts[level];
  }
}
