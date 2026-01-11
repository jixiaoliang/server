package com.chanchan.game.battle;

public class Rng {
  private long state;

  public Rng(int seed) {
    this.state = seed & 0xffffffffL;
    if (this.state == 0) {
      this.state = 1;
    }
  }

  public double next() {
    state = (state * 1664525 + 1013904223) & 0xffffffffL;
    return (double) state / 4294967296.0;
  }
}
