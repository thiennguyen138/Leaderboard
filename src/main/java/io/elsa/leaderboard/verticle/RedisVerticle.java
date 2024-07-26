package io.elsa.leaderboard.verticle;

import io.elsa.leaderboard.common.VertXAddress;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import io.vertx.redis.client.Response;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class RedisVerticle extends AbstractVerticle {
  private volatile static RedisAPI redisAPI;
  private static String LEADERBOARD = "LEADERBOARD";
  private Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

  @Override
  public void start() {
    RedisOptions options = new RedisOptions();
    options.setConnectionString("redis://:Passw0rd@127.0.0.1:6379/0");
    options.setMaxPoolSize(20);
    Redis.createClient(vertx, options)
      .connect()
      .onFailure(t -> logger.info(ExceptionUtils.getStackTrace(t)))
      .onSuccess(conn -> {
        logger.info("Init redis connection successfully");

        redisAPI = RedisAPI.api(conn);

        vertx.eventBus().consumer(VertXAddress.GET_RANK, message -> redisAPI.zrevrank(LEADERBOARD, message.body().toString(), rs -> {
          if (rs.succeeded()) {
            message.reply(rs.result() != null ? rs.result().toLong() : -1l);
          } else {
            message.fail(500, ExceptionUtils.getStackTrace(rs.cause()));
          }
        }));

        vertx.eventBus().consumer(VertXAddress.GET_RANKS, message -> {
          JsonObject body = (JsonObject) message.body();
          redisAPI.zrange(Arrays.asList(LEADERBOARD, body.getString("from"), body.getString("to")), rs -> {
            if (rs.succeeded()) {
              if (rs.result() != null) {
                message.reply(rs.result().stream().map(Response::toString).toList());
              } else {
                message.reply(null);
              }
            } else {
              message.fail(500, ExceptionUtils.getStackTrace(rs.cause()));
            }
          });
        });

        vertx.eventBus().consumer(VertXAddress.GET_RELATIVE_RANK, message -> {
          JsonObject body = (JsonObject) message.body();
          redisAPI.zrevrange(Arrays.asList(LEADERBOARD, body.getString("fromRank"), body.getString("toRank")), rs -> {
            if (rs.succeeded()) {
              message.reply(rs.result().stream().map(Response::toString).toList());
            } else {
              message.fail(500, ExceptionUtils.getStackTrace(rs.cause()));
            }
          });
        });
      });
  }
}
