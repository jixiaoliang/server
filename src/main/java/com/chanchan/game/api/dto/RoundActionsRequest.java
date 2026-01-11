package com.chanchan.game.api.dto;

import java.util.List;

public record RoundActionsRequest(
  String matchId,
  int round,
  List<String> buy,
  List<String> sell,
  List<MoveDto> move,
  int refreshCount,
  int levelUp,
  List<BoardUnitDto> board
) {}
