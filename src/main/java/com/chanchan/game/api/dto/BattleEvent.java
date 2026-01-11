package com.chanchan.game.api.dto;

import java.util.Map;

public record BattleEvent(String type, long ts, Map<String, Object> payload) {}
