package com.chanchan.game.match;

import java.util.List;

public class PlayerState {
  private int gold;
  private int level;
  private List<String> shop;

  public PlayerState(int gold, int level) {
    this.gold = gold;
    this.level = level;
  }

  public int getGold() {
    return gold;
  }

  public void setGold(int gold) {
    this.gold = gold;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public List<String> getShop() {
    return shop;
  }

  public void setShop(List<String> shop) {
    this.shop = shop;
  }
}
