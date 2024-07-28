package io.elsa.leaderboard.verticle;

import io.elsa.leaderboard.common.StringUtil;
import io.elsa.leaderboard.common.VertXAddress;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;
import io.vertx.sqlclient.*;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.*;

public class PostgresVerticle extends AbstractVerticle {

    private SqlClient sqlClient;

    @Override
    public void start() {
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(5432)
                .setHost(config().getString("postgresHost"))
                .setDatabase("postgres")
                .setUser("postgres")
                .setPassword("postgres");

        Map<String, String> props = new HashMap<>();
        props.put("search_path", "leaderboard");
        connectOptions.setProperties(props);

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(20);

        sqlClient = PgBuilder
                .client()
                .with(poolOptions)
                .connectingTo(connectOptions)
                .using(vertx)
                .build();

        vertx.eventBus().consumer(VertXAddress.GET_USERS_SCORE, message -> {
            JsonArray userIds = (JsonArray) message.body();
            sqlClient.query("select * from leaderboard.user_scores where user_id IN (" + StringUtil.parseListStringToString(userIds.getList()) + ")")
                    .execute()
                    .onComplete(ar -> {
                        if (ar.succeeded()) {
                            JsonArray jsonArray = new JsonArray();
                            ar.result().forEach(rs -> jsonArray.add(rs.toJson()));
                            message.reply(jsonArray);
                        } else {
                            message.fail(500, "Internal Server Error");
                        }
                    });
        });

    }
}
