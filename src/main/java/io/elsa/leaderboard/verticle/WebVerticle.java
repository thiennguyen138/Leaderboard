package io.elsa.leaderboard.verticle;

import io.elsa.leaderboard.service.LeaderBoardService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebVerticle extends AbstractVerticle {

    private static volatile Router router;
    private static LeaderBoardService leaderBoardService;
    private final Logger logger = LoggerFactory.getLogger(WebVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        HttpServer server = vertx.createHttpServer();
        router = Router.router(vertx);
        server.requestHandler(router).listen(8090);
        leaderBoardService = LeaderBoardService.createProxy(vertx);

        getTopRankOnLeaderboard();
        getMyRank();
        getRelativeRank();
    }

    private void getTopRankOnLeaderboard() {
        logger.info("Init getTopRankOnLeaderboard controller");
        router
                .route(HttpMethod.GET, "/leaderboard")
                .handler(ctx -> {
                    MultiMap params = ctx.request().params();
                    int from = Integer.parseInt(params.get("from"));
                    int to = Integer.parseInt(params.get("to"));

                    leaderBoardService.getLeaderboardInRange(from, to, rs -> {
                        if (rs.succeeded()) {
                            HttpServerResponse response = ctx.response();
                            response.setChunked(true);
                            response.write(rs.result());
                            ctx.response().end();
                        } else {
                            ctx.response().setStatusCode(500).end("Internal Server Error");
                        }
                    });
                }).failureHandler(ctx -> {
                    logger.error("Got Error");
                    ctx.response().setStatusCode(ctx.statusCode()).end("Error occurred in method");
                });
        ;
    }

    private void getMyRank() {
        logger.info("Init getMyRank controller");
        router
                .route(HttpMethod.GET, "/leaderboard/:userId")
                .handler(ctx -> {
                    leaderBoardService.getRankByUserId(ctx.pathParam("userId"), rs -> {
                        if (rs.succeeded()) {
                            HttpServerResponse response = ctx.response();
                            response.setChunked(true);
                            if (rs.result() != null) {
                                response.write(rs.result().toString());
                            }
                            ctx.response().end();
                        } else {
                            logger.error("Got Error");
                            ctx.response().setStatusCode(500).end("Internal Server Error");
                        }
                    });
                });
    }

    private void getRelativeRank() {
        logger.info("Init getRelativeRank controller");
        router
                .route(HttpMethod.GET, "/leaderboard/rel/:userId")
                .handler(ctx -> {
                    String userId = ctx.pathParam("userId");
                    MultiMap params = ctx.request().params();
                    int bound = Integer.parseInt(params.get("bound"));

                    leaderBoardService.getRankByUserId(userId, rs -> {
                        if (rs.succeeded()) {
                            HttpServerResponse response = ctx.response();
                            response.setChunked(true);
                            if (rs.result() != null) {
                                leaderBoardService.getLeaderboardInRange((int) (rs.result() - bound), (int) (rs.result() + bound), relRs -> {
                                    if (relRs.succeeded() && relRs.result() != null) {
                                        response.write(relRs.result());
                                        ctx.response().end();
                                    } else {
                                        ctx.response().setStatusCode(500).end("Internal Server Error");
                                    }
                                });
                            } else {
                                ctx.response().end("[]");
                            }
                        } else {
                            logger.error("Got Error");
                            ctx.response().setStatusCode(500).end("Internal Server Error");
                        }
                    });
                });
    }
}
