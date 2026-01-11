package com.chanchan.game.match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class ShopService {
  private final List<String> unitPool;
  private final Map<Integer, List<CostOdds>> oddsByLevel;

  public ShopService() {
    unitPool = new ArrayList<>();
    for (int i = 1; i <= 30; i += 1) {
      unitPool.add(String.format("u_%03d", i));
    }
    oddsByLevel = new HashMap<>();
    oddsByLevel.put(1, List.of(new CostOdds(1, 1.0)));
    oddsByLevel.put(2, List.of(new CostOdds(1, 1.0)));
    oddsByLevel.put(3, List.of(new CostOdds(1, 0.75), new CostOdds(2, 0.25)));
    oddsByLevel.put(4, List.of(new CostOdds(1, 0.55), new CostOdds(2, 0.3), new CostOdds(3, 0.15)));
    oddsByLevel.put(5, List.of(new CostOdds(1, 0.45), new CostOdds(2, 0.33), new CostOdds(3, 0.2), new CostOdds(4, 0.02)));
    oddsByLevel.put(6, List.of(new CostOdds(1, 0.3), new CostOdds(2, 0.4), new CostOdds(3, 0.25), new CostOdds(4, 0.05)));
    oddsByLevel.put(7, List.of(new CostOdds(1, 0.2), new CostOdds(2, 0.33), new CostOdds(3, 0.3), new CostOdds(4, 0.15), new CostOdds(5, 0.02)));
    oddsByLevel.put(8, List.of(new CostOdds(1, 0.15), new CostOdds(2, 0.2), new CostOdds(3, 0.35), new CostOdds(4, 0.22), new CostOdds(5, 0.08)));
    oddsByLevel.put(9, List.of(new CostOdds(1, 0.1), new CostOdds(2, 0.15), new CostOdds(3, 0.3), new CostOdds(4, 0.3), new CostOdds(5, 0.15)));
  }

  public List<String> rollShop(int level, int slots) {
    List<CostOdds> odds = oddsByLevel.getOrDefault(level, oddsByLevel.get(1));
    List<String> result = new ArrayList<>();
    for (int i = 0; i < slots; i += 1) {
      int cost = pickCost(odds);
      result.add(pickUnitByCost(cost));
    }
    return result;
  }

  private int pickCost(List<CostOdds> odds) {
    double roll = ThreadLocalRandom.current().nextDouble();
    double acc = 0.0;
    for (CostOdds o : odds) {
      acc += o.rate();
      if (roll <= acc) return o.cost();
    }
    return odds.get(odds.size() - 1).cost();
  }

  private String pickUnitByCost(int cost) {
    List<String> pool = new ArrayList<>();
    for (String unit : unitPool) {
      int unitCost = UnitCost.estimate(unit);
      if (unitCost == cost) pool.add(unit);
    }
    return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
  }

  private record CostOdds(int cost, double rate) {}
}
