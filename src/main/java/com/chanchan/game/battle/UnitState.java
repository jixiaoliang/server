package com.chanchan.game.battle;

public class UnitState {
  private final String id;
  private final String side;
  private final int atk;
  private final double atkSpd;
  private final int range;
  private final Skill skill;
  private int hp;
  private boolean alive;
  private int cooldown;
  private int skillCd;
  private Position pos;

  public UnitState(String id, String side, int hp, int atk, double atkSpd, int range, Position pos, Skill skill) {
    this.id = id;
    this.side = side;
    this.hp = hp;
    this.atk = atk;
    this.atkSpd = atkSpd;
    this.range = range;
    this.pos = pos;
    this.skill = skill;
    this.alive = true;
    this.cooldown = 0;
    this.skillCd = skill != null ? skill.getCooldownMs() : Integer.MAX_VALUE;
  }

  public String getId() {
    return id;
  }

  public String getSide() {
    return side;
  }

  public int getAtk() {
    return atk;
  }

  public double getAtkSpd() {
    return atkSpd;
  }

  public int getRange() {
    return range;
  }

  public int getHp() {
    return hp;
  }

  public void setHp(int hp) {
    this.hp = hp;
  }

  public boolean isAlive() {
    return alive;
  }

  public void setAlive(boolean alive) {
    this.alive = alive;
  }

  public int getCooldown() {
    return cooldown;
  }

  public void setCooldown(int cooldown) {
    this.cooldown = cooldown;
  }

  public int getSkillCd() {
    return skillCd;
  }

  public void setSkillCd(int skillCd) {
    this.skillCd = skillCd;
  }

  public Position getPos() {
    return pos;
  }

  public void setPos(Position pos) {
    this.pos = pos;
  }

  public Skill getSkill() {
    return skill;
  }
}
