package com.chanchan.game.battle;

import com.chanchan.game.api.dto.BattleEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BattleSim {
  private static final int DEFAULT_TICK_MS = 200;
  private static final int MAX_TICKS = 300;
  private static final int BOARD_WIDTH = 7;
  private static final int BOARD_HEIGHT = 4;

  public BattleResult simulate(List<UnitState> teamA, List<UnitState> teamB, int seed) {
    List<UnitState> units = new ArrayList<>();
    units.addAll(teamA);
    units.addAll(teamB);

    Rng rng = new Rng(seed);
    List<BattleEvent> events = new ArrayList<>();

    events.add(event("battle_start", 0, Map.of("tickMs", DEFAULT_TICK_MS, "seed", seed)));
    for (UnitState unit : units) {
      Map<String, Object> payload = new HashMap<>();
      payload.put("id", unit.getId());
      payload.put("side", unit.getSide());
      payload.put("hp", unit.getHp());
      payload.put("pos", Map.of("x", unit.getPos().x(), "y", unit.getPos().y()));
      events.add(event("unit_spawn", 0, payload));
      unit.setCooldown((int) Math.floor(200 + rng.next() * 400));
      if (unit.getSkill() != null) {
        unit.setSkillCd((int) Math.floor(rng.next() * unit.getSkill().getCooldownMs()));
      }
    }

    int tick = 0;
    while (tick < MAX_TICKS && hasLiving(units, "A") && hasLiving(units, "B")) {
      for (UnitState unit : units) {
        if (!unit.isAlive()) continue;
        unit.setCooldown(unit.getCooldown() - DEFAULT_TICK_MS);
        unit.setSkillCd(unit.getSkillCd() - DEFAULT_TICK_MS);

        UnitState target = pickNearestTarget(units, unit.getSide(), unit.getPos());
        if (target == null) continue;

        int dist = distance(unit.getPos(), target.getPos());
        if (dist > unit.getRange()) {
          Position next = stepToward(unit.getPos(), target.getPos(), rng);
          if (next != null) {
            unit.setPos(next);
            events.add(event("unit_move", tick, Map.of("id", unit.getId(), "pos", Map.of("x", next.x(), "y", next.y()))));
          }
          continue;
        }

        if (unit.getSkill() != null && unit.getSkillCd() <= 0) {
          Skill skill = unit.getSkill();
          if ("aoe".equals(skill.getType())) {
            for (UnitState enemy : units) {
              if (!enemy.isAlive() || enemy.getSide().equals(unit.getSide())) continue;
              if (distance(enemy.getPos(), target.getPos()) <= skill.getRadius()) {
                enemy.setHp(enemy.getHp() - skill.getDamage());
                events.add(event("unit_hit", tick, Map.of("source", unit.getId(), "target", enemy.getId(), "damage", skill.getDamage(), "kind", "skill")));
                if (enemy.getHp() <= 0) {
                  enemy.setAlive(false);
                  events.add(event("unit_death", tick, Map.of("unit", enemy.getId())));
                }
              }
            }
          } else {
            target.setHp(target.getHp() - skill.getDamage());
            events.add(event("unit_hit", tick, Map.of("source", unit.getId(), "target", target.getId(), "damage", skill.getDamage(), "kind", "skill")));
            if (target.getHp() <= 0) {
              target.setAlive(false);
              events.add(event("unit_death", tick, Map.of("unit", target.getId())));
            }
          }
          events.add(event("unit_cast", tick, Map.of("source", unit.getId(), "target", target.getId(), "type", skill.getType(), "damage", skill.getDamage())));
          unit.setSkillCd(skill.getCooldownMs());
          continue;
        }

        if (unit.getCooldown() <= 0) {
          target.setHp(target.getHp() - unit.getAtk());
          events.add(event("unit_attack", tick, Map.of("source", unit.getId(), "target", target.getId(), "damage", unit.getAtk())));
          if (target.getHp() <= 0) {
            target.setAlive(false);
            events.add(event("unit_death", tick, Map.of("unit", target.getId())));
          }
          unit.setCooldown(Math.max(200, (int) Math.floor(1000 / unit.getAtkSpd())));
        }
      }
      tick += DEFAULT_TICK_MS;
    }

    String winner = hasLiving(units, "A") ? "A" : "B";
    events.add(event("battle_end", tick, Map.of("winner", winner)));
    return new BattleResult(winner, events);
  }

  private boolean hasLiving(List<UnitState> units, String side) {
    for (UnitState unit : units) {
      if (unit.isAlive() && unit.getSide().equals(side)) return true;
    }
    return false;
  }

  private UnitState pickNearestTarget(List<UnitState> units, String side, Position pos) {
    UnitState chosen = null;
    int best = Integer.MAX_VALUE;
    for (UnitState unit : units) {
      if (!unit.isAlive() || unit.getSide().equals(side)) continue;
      int dist = distance(pos, unit.getPos());
      if (dist < best) {
        best = dist;
        chosen = unit;
      }
    }
    return chosen;
  }

  private int distance(Position a, Position b) {
    return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
  }

  private Position stepToward(Position pos, Position target, Rng rng) {
    List<Position> options = new ArrayList<>();
    if (pos.x() < target.x()) options.add(new Position(pos.x() + 1, pos.y()));
    if (pos.x() > target.x()) options.add(new Position(pos.x() - 1, pos.y()));
    if (pos.y() < target.y()) options.add(new Position(pos.x(), pos.y() + 1));
    if (pos.y() > target.y()) options.add(new Position(pos.x(), pos.y() - 1));

    List<Position> valid = new ArrayList<>();
    for (Position option : options) {
      if (option.x() >= 0 && option.x() < BOARD_WIDTH && option.y() >= 0 && option.y() < BOARD_HEIGHT) {
        valid.add(option);
      }
    }

    if (valid.isEmpty()) return pos;
    int idx = (int) Math.floor(rng.next() * valid.size());
    return valid.get(idx);
  }

  private BattleEvent event(String type, long ts, Map<String, Object> payload) {
    return new BattleEvent(type, ts, payload);
  }
}
