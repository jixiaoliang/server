package com.chanchan.game.match;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class MatchStore {
  private final Map<String, MatchState> matches = new ConcurrentHashMap<>();

  public MatchState create(String matchId, int seed) {
    MatchState state = new MatchState(matchId, seed);
    matches.put(matchId, state);
    return state;
  }

  public MatchState get(String matchId) {
    if (matchId == null) return null;
    return matches.get(matchId);
  }
}
