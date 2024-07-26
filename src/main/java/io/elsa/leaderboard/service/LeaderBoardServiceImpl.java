package io.elsa.leaderboard.service;

import io.elsa.leaderboard.common.VertXAddress;
import io.elsa.leaderboard.common.VertXInstance;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class LeaderBoardServiceImpl implements LeaderBoardService {

  @Override
  public void getLeaderboardInRange(int from, int to, Handler<AsyncResult<List<String>>> result) {
    JsonObject body = new JsonObject();
    body.put("from", from);
    body.put("to", to);
    VertXInstance.vertx.eventBus().request(VertXAddress.GET_RANKS, body, rs -> {
      if (rs.succeeded() && rs.result() != null) {
        List<String> ranks = (List<String>) rs.result().body();
        result.handle(rs.map(ranks));
      } else {
        result.handle(rs.mapEmpty());
      }
    });
  }

  @Override
  public void getRankByUserId(String userId, Handler<AsyncResult<Long>> result) {
    VertXInstance.vertx.eventBus().request(VertXAddress.GET_RANK, userId, rs -> {
      if (rs.succeeded() && rs.result().body() != null) {
        result.handle(rs.map((Long) rs.result().body()));
      } else {
        result.handle(rs.mapEmpty());
      }
    });
  }

  @Override
  public void getRelativeLeaderboard(long fromRank, long toRank, Handler<AsyncResult<List<String>>> result) {
    JsonObject body = new JsonObject();
    body.put("fromRank", fromRank);
    body.put("toRank", toRank);
    VertXInstance.vertx.eventBus().request(VertXAddress.GET_RELATIVE_RANK, body, rs -> {
      if (rs.succeeded() && rs.result().body() != null) {
        result.handle(rs.map((List<String>) rs.result().body()));
      } else {
        result.handle(rs.mapEmpty());
      }
    });
  }
}
