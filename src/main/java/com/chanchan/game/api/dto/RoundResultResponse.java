package com.chanchan.game.api.dto;

import java.util.List;

public record RoundResultResponse(
  int round,
  String winner,
  List<BattleEvent> battleEvents,
  List<String> moveErrors,
  List<Integer> invalidIndices,
  boolean accepted
) {}
