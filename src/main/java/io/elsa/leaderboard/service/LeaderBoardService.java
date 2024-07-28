package io.elsa.leaderboard.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

@ProxyGen
public interface LeaderBoardService {
  String LEADERBOARD_SERVICE_ADDRESS = "leaderboard_service";

  static LeaderBoardService createProxy(Vertx vertx) {
    return new LeaderBoardServiceVertxEBProxy(vertx, LEADERBOARD_SERVICE_ADDRESS);
  }

  void getLeaderboardInRange(int from, int to, Handler<AsyncResult<String>> result);

  void getRankByUserId(String userId, Handler<AsyncResult<Long>> result);

  void getUserScore(List<String> userIds, Handler<AsyncResult<String>> result);

}
