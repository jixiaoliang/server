package com.chanchan.game.api;

import com.chanchan.game.api.dto.MatchJoinRequest;
import com.chanchan.game.api.dto.MatchReadyResponse;
import com.chanchan.game.match.MatchState;
import com.chanchan.game.match.MatchStore;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/match")
public class MatchController {
  private final MatchStore matchStore;

  public MatchController(MatchStore matchStore) {
    this.matchStore = matchStore;
  }

  @PostMapping("/join")
  public MatchReadyResponse join(@RequestBody MatchJoinRequest request) {
    String matchId = "m_" + UUID.randomUUID().toString().replace("-", "");
    int seed = ThreadLocalRandom.current().nextInt(1, 100000);
    MatchState state = matchStore.create(matchId, seed);
    return new MatchReadyResponse(state.getMatchId(), state.getSeed(), request.mode());
  }
}
