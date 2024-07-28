package io.elsa.leaderboard.service;

import io.elsa.leaderboard.common.VertXAddress;
import io.elsa.leaderboard.common.VertXInstance;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class LeaderBoardServiceImpl implements LeaderBoardService {

    @Override
    public void getLeaderboardInRange(int from, int to, Handler<AsyncResult<String>> result) {
        if (to <= 1000) {
            JsonObject body = new JsonObject();
            body.put("key", "LEADERBOARD_CACHE");
            body.put("field", from + "," + to);
            VertXInstance.vertx.eventBus().request(VertXAddress.GET_FROM_CACHE, body, rs -> {
                if (rs.succeeded()) {
                    if (rs.result() == null || rs.result().body() == null) {
                        getLeaderboardInRangeAndUpdateCache(from, to, result);
                    } else {
                        result.handle(rs.map((String) rs.result().body()));
                    }
                } else {
                    result.handle(rs.mapEmpty());
                }
            });
        } else {
            getLeaderboardInRangeAndUpdateCache(from, to, result);
        }
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
    public void getUserScore(List<String> userIds, Handler<AsyncResult<String>> result) {
        JsonArray userIdJsonArrays = new JsonArray();
        userIds.forEach(userIdJsonArrays::add);
        VertXInstance.vertx.eventBus().request(VertXAddress.GET_USERS_SCORE, userIdJsonArrays, rs -> {
            if (rs.succeeded() && rs.result().body() != null) {
                JsonArray jsonArray = (JsonArray) rs.result().body();
                result.handle(rs.map(jsonArray.toString()));
            } else {
                result.handle(rs.mapEmpty());
            }
        });
    }

    public void getLeaderboardInRangeAndUpdateCache(int from, int to, Handler<AsyncResult<String>> result) {
        JsonObject body = new JsonObject();
        body.put("from", from);
        body.put("to", to);
        VertXInstance.vertx.eventBus().request(VertXAddress.GET_RANKS, body, rs -> {
            if (rs.succeeded() && rs.result() != null) {
                List<String> userIds = (List<String>) rs.result().body();

                JsonArray userIdJsonArrays = new JsonArray();
                userIds.forEach(userIdJsonArrays::add);
                VertXInstance.vertx.eventBus().request(VertXAddress.GET_USERS_SCORE, userIdJsonArrays, usrs -> {
                    if (usrs.succeeded() && usrs.result().body() != null) {
                        String userScores = usrs.result().body().toString();
                        if (to <= 1000) {
                            putToCache(from + "," + to, userScores);
                        }
                        result.handle(usrs.map(userScores));
                    } else {
                        result.handle(usrs.mapEmpty());
                    }
                });
            } else {
                result.handle(rs.mapEmpty());
            }
        });
    }

    public void putToCache(String field, String value) {
        JsonObject body = new JsonObject();
        body.put("key", "LEADERBOARD_CACHE");
        body.put("field", field);
        body.put("value", value);
        VertXInstance.vertx.eventBus().send(VertXAddress.PUT_TO_CACHE, body);

    }

}
