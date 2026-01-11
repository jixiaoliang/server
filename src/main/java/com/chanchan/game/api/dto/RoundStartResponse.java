package com.chanchan.game.api.dto;

import java.util.List;

public record RoundStartResponse(int round, List<String> shop, int gold, int level) {}
