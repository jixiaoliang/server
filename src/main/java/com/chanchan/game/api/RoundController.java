package com.chanchan.game.api;

import com.chanchan.game.api.dto.BoardUnitDto;
import com.chanchan.game.api.dto.MoveDto;
import com.chanchan.game.api.dto.RoundActionsRequest;
import com.chanchan.game.api.dto.RoundResultResponse;
import com.chanchan.game.api.dto.RoundStartResponse;
import com.chanchan.game.battle.BattleResult;
import com.chanchan.game.battle.BattleSim;
import com.chanchan.game.battle.Position;
import com.chanchan.game.battle.Skill;
import com.chanchan.game.battle.UnitState;
import com.chanchan.game.match.MatchState;
import com.chanchan.game.match.MatchStore;
import com.chanchan.game.match.EconomyRules;
import com.chanchan.game.match.PlayerState;
import com.chanchan.game.match.ShopService;
import com.chanchan.game.match.UnitCost;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/round")
public class RoundController {
  private final MatchStore matchStore;
  private final ShopService shopService;
  private final EconomyRules economyRules = new EconomyRules();

  public RoundController(MatchStore matchStore, ShopService shopService) {
    this.matchStore = matchStore;
    this.shopService = shopService;
  }

  @GetMapping("/start")
  public RoundStartResponse start(@RequestParam(name = "round", defaultValue = "1") int round,
                                  @RequestParam(name = "matchId", required = false) String matchId) {
    MatchState state = matchStore.get(matchId);
    if (state == null) {
      List<String> shop = List.of("u_001", "u_006", "u_011", "u_016", "u_021");
      return new RoundStartResponse(round, shop, 10, 3);
    }
    state.setRound(round);
    PlayerState player = state.getPlayer();
    if (player.getShop() == null) {
      player.setShop(shopService.rollShop(player.getLevel(), 5));
    }
    return new RoundStartResponse(round, player.getShop(), player.getGold(), player.getLevel());
  }

  @PostMapping("/submit")
  public RoundResultResponse submit(@RequestBody RoundActionsRequest request) {
    int seed = ThreadLocalRandom.current().nextInt(1, 100000);
    BattleSim sim = new BattleSim();
    MatchState state = matchStore.get(request.matchId());
    List<BoardUnitDto> board = request.board();
    if ((board == null || board.isEmpty()) && state != null) {
      board = state.getBoard();
    }
    MoveValidation moveValidation = validateMoves(request.move(), board);
    MoveValidation boardValidation = validateBoard(board);
    MoveValidation mergedValidation = mergeValidations(moveValidation, boardValidation);
    List<UnitState> teamA = buildTeamFromRequest(board, "A");
    if (teamA.isEmpty()) {
      teamA = sampleTeam("A");
    }
    if (!mergedValidation.errors().isEmpty()) {
      return new RoundResultResponse(
        request.round(),
        "N/A",
        List.of(),
        mergedValidation.errors(),
        mergedValidation.invalidIndices(),
        false
      );
    }

    BattleResult result = sim.simulate(teamA, sampleTeam("B"), seed);
    if (state != null) {
      applyEconomy(state.getPlayer(), request);
      applyShopActions(state.getPlayer(), request);
      state.setRound(request.round());
      state.setBoard(board);
      state.setLastMoves(request.move());
    }
    return new RoundResultResponse(
      request.round(),
      result.winner(),
      result.events(),
      mergedValidation.errors(),
      mergedValidation.invalidIndices(),
      true
    );
  }

  private MoveValidation validateMoves(List<MoveDto> moves, List<BoardUnitDto> board) {
    List<String> errors = new ArrayList<>();
    List<Integer> invalidIndices = new ArrayList<>();
    if (moves == null || board == null) return new MoveValidation(errors, invalidIndices);

    java.util.Map<String, BoardUnitDto> unitMap = new java.util.HashMap<>();
    java.util.Set<String> occupied = new java.util.HashSet<>();
    for (BoardUnitDto unit : board) {
      unitMap.put(unit.id(), unit);
      if (unit.pos() != null) {
        occupied.add(unit.pos().x() + "," + unit.pos().y());
      }
    }

    for (MoveDto move : moves) {
      if (move == null || move.id() == null || move.to() == null) {
        errors.add("invalid_move");
        continue;
      }
      if (move.to().x() < 0 || move.to().x() >= 7 || move.to().y() < 0 || move.to().y() >= 4) {
        errors.add("out_of_bounds:" + move.id());
        invalidIndices.add(move.to().y() * 7 + move.to().x());
        continue;
      }
      BoardUnitDto unit = unitMap.get(move.id());
      if (unit == null) {
        errors.add("missing_unit:" + move.id());
        continue;
      }
      String key = move.to().x() + "," + move.to().y();
      if (occupied.contains(key) && (unit.pos() == null || unit.pos().x() != move.to().x() || unit.pos().y() != move.to().y())) {
        errors.add("occupied:" + move.id());
        invalidIndices.add(move.to().y() * 7 + move.to().x());
        continue;
      }
      occupied.remove(unit.pos() == null ? "" : unit.pos().x() + "," + unit.pos().y());
      occupied.add(key);
    }

    return new MoveValidation(errors, invalidIndices);
  }

  private MoveValidation validateBoard(List<BoardUnitDto> board) {
    List<String> errors = new ArrayList<>();
    List<Integer> invalidIndices = new ArrayList<>();
    if (board == null) return new MoveValidation(errors, invalidIndices);

    java.util.Set<String> occupied = new java.util.HashSet<>();
    for (BoardUnitDto unit : board) {
      if (unit == null || unit.pos() == null) {
        errors.add("missing_pos");
        continue;
      }
      int x = unit.pos().x();
      int y = unit.pos().y();
      if (x < 0 || x >= 7 || y < 0 || y >= 4) {
        errors.add("out_of_bounds:" + unit.id());
        invalidIndices.add(y * 7 + x);
        continue;
      }
      String key = x + "," + y;
      if (occupied.contains(key)) {
        errors.add("duplicate_pos:" + unit.id());
        invalidIndices.add(y * 7 + x);
        continue;
      }
      occupied.add(key);
    }

    return new MoveValidation(errors, invalidIndices);
  }

  private MoveValidation mergeValidations(MoveValidation a, MoveValidation b) {
    List<String> errors = new ArrayList<>();
    List<Integer> invalidIndices = new ArrayList<>();
    if (a != null) {
      errors.addAll(a.errors());
      invalidIndices.addAll(a.invalidIndices());
    }
    if (b != null) {
      errors.addAll(b.errors());
      invalidIndices.addAll(b.invalidIndices());
    }
    return new MoveValidation(errors, invalidIndices);
  }

  private record MoveValidation(List<String> errors, List<Integer> invalidIndices) {}

  private void applyShopActions(PlayerState player, RoundActionsRequest request) {
    if (player == null) return;
    if (request.refreshCount() > 0 || player.getShop() == null) {
      player.setShop(shopService.rollShop(player.getLevel(), 5));
    }
    List<String> shop = player.getShop();
    if (shop == null || request.buy() == null) return;
    for (String unitId : request.buy()) {
      for (int i = 0; i < shop.size(); i += 1) {
        if (shop.get(i).equals(unitId)) {
          shop.remove(i);
          break;
        }
      }
    }
  }

  private void applyEconomy(PlayerState player, RoundActionsRequest request) {
    if (player == null) return;
    int gold = player.getGold();
    if (request.buy() != null) {
      for (String unitId : request.buy()) {
        gold -= UnitCost.estimate(unitId);
      }
    }
    if (request.sell() != null) {
      for (String unitId : request.sell()) {
        gold += UnitCost.estimate(unitId);
      }
    }
    int refreshCount = Math.max(0, request.refreshCount());
    if (refreshCount > 0) {
      gold -= refreshCount * economyRules.refreshCost;
    }
    int levelUp = Math.max(0, request.levelUp());
    if (levelUp > 0) {
      int cost = economyRules.levelUpCost(player.getLevel());
      if (gold >= cost) {
        gold -= cost;
        player.setLevel(Math.min(9, player.getLevel() + 1));
      }
    }
    gold = Math.max(0, gold);
    gold += economyRules.baseGoldPerRound + economyRules.calcInterest(gold);
    player.setGold(gold);
  }

  private List<UnitState> buildTeamFromRequest(List<BoardUnitDto> board, String side) {
    List<UnitState> team = new ArrayList<>();
    if (board == null) return team;
    for (BoardUnitDto unit : board) {
      if (unit.stats() == null || unit.pos() == null) continue;
      Skill skill = null;
      if (unit.skill() != null) {
        skill = new Skill(unit.skill().type(), unit.skill().damage(), unit.skill().radius(), unit.skill().cooldownMs());
      }
      team.add(new UnitState(
        unit.id(),
        side,
        unit.stats().hp(),
        unit.stats().atk(),
        unit.stats().atkSpd(),
        unit.stats().range(),
        new Position(unit.pos().x(), unit.pos().y()),
        skill
      ));
    }
    return team;
  }

  private List<UnitState> sampleTeam(String side) {
    List<UnitState> team = new ArrayList<>();
    if ("A".equals(side)) {
      team.add(new UnitState("a_1", "A", 520, 45, 0.7, 1, new Position(2, 3), new Skill("single", 120, 0, 2400)));
      team.add(new UnitState("a_2", "A", 480, 52, 0.65, 1, new Position(4, 3), null));
    } else {
      team.add(new UnitState("b_1", "B", 500, 48, 0.68, 1, new Position(2, 0), null));
      team.add(new UnitState("b_2", "B", 460, 54, 0.62, 1, new Position(4, 0), null));
    }
    return team;
  }
}
