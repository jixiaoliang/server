package com.chanchan.game.battle;

import com.chanchan.game.api.dto.BattleEvent;
import java.util.List;

public record BattleResult(String winner, List<BattleEvent> events) {}
