package com.chanchan.game.battle;

public class Skill {
  private final String type;
  private final int damage;
  private final int radius;
  private final int cooldownMs;

  public Skill(String type, int damage, int radius, int cooldownMs) {
    this.type = type;
    this.damage = damage;
    this.radius = radius;
    this.cooldownMs = cooldownMs;
  }

  public String getType() {
    return type;
  }

  public int getDamage() {
    return damage;
  }

  public int getRadius() {
    return radius;
  }

  public int getCooldownMs() {
    return cooldownMs;
  }
}
