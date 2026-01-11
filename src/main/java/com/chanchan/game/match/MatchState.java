package com.chanchan.game.match;

import com.chanchan.game.api.dto.BoardUnitDto;
import com.chanchan.game.api.dto.MoveDto;
import java.util.List;

public class MatchState {
  private final String matchId;
  private final int seed;
  private int round;
  private List<BoardUnitDto> board;
  private List<MoveDto> lastMoves;
  private final PlayerState player;

  public MatchState(String matchId, int seed) {
    this.matchId = matchId;
    this.seed = seed;
    this.round = 1;
    this.player = new PlayerState(10, 3);
  }

  public String getMatchId() {
    return matchId;
  }

  public int getSeed() {
    return seed;
  }

  public int getRound() {
    return round;
  }

  public void setRound(int round) {
    this.round = round;
  }

  public List<BoardUnitDto> getBoard() {
    return board;
  }

  public void setBoard(List<BoardUnitDto> board) {
    this.board = board;
  }

  public PlayerState getPlayer() {
    return player;
  }

  public List<MoveDto> getLastMoves() {
    return lastMoves;
  }

  public void setLastMoves(List<MoveDto> lastMoves) {
    this.lastMoves = lastMoves;
  }
}
