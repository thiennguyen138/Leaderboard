package io.elsa.leaderboard.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.*;

import java.util.*;

public class PostgresVerticle extends AbstractVerticle {
  @Override
  public void start() {
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("postgres")
      .setDatabase("postgres")
      .setUser("postgres")
      .setPassword("postgres");

    Map<String, String> props = new HashMap<>();
    props.put("search_path", "test");
    connectOptions.setProperties(props);

    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(20);

    SqlClient client = PgBuilder
      .client()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .using(vertx)
      .build();

//    int leftLimit = 97; // letter 'a'
//    int rightLimit = 122; // letter 'z'
//    int targetStringLength = 10;
//    Random random = new Random();
//
//    for (int i = 0; i < 500; i++) {
//      List<Tuple> batch = new ArrayList<>();
//      for (int j = 0; j < 100000; j++) {
//        String generatedUserId = random.ints(leftLimit, rightLimit + 1)
//          .limit(targetStringLength)
//          .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
//          .toString();
//
//        String generatedGameId = random.ints(leftLimit, rightLimit + 1)
//          .limit(1)
//          .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
//          .toString();
//
//        batch.add(Tuple.of(generatedUserId, random.nextInt(9999), generatedGameId));
//      }
//      client
//        .preparedQuery("INSERT INTO test.user_score(user_id, score, game_id) VALUES ($1, $2, $3)")
//        .executeBatch(batch)
//        .onComplete(ar -> {
//          if (ar.succeeded()) {
//            RowSet<Row> rows = ar.result();
//            System.out.println(rows.rowCount());
//          } else {
//            System.out.println("Failure: " + ar.cause().getMessage());
//          }
//        });
//    }

  }
}
