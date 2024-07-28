package io.elsa.leaderboard;

import com.hazelcast.config.Config;
import io.elsa.leaderboard.common.VertXInstance;
import io.elsa.leaderboard.service.LeaderBoardService;
import io.elsa.leaderboard.service.LeaderBoardServiceImpl;
import io.elsa.leaderboard.verticle.PostgresVerticle;
import io.elsa.leaderboard.verticle.RedisVerticle;
import io.elsa.leaderboard.verticle.WebVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("please specify postgres host and redis host");
            return;
        }
        String logFactory = System.getProperty("org.vertx.logger-delegate-factory-class-name");
        if (logFactory == null) {
            System.setProperty("org.vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
        }
        ClusterManager mgr = new HazelcastClusterManager(hazelCastConfig());
        VertxOptions options = new VertxOptions()
                .setQuorumSize(1)
                .setEventBusOptions(new EventBusOptions()
                        .setHost("0.0.0.0")
                        .setPort(4242)
                        .setClusterPublicHost("127.0.0.1")
                        .setClusterPublicPort(4242))
                .setPreferNativeTransport(true);
        Vertx.builder().with(options).withClusterManager(mgr).buildClustered(res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                VertXInstance.vertx = vertx;
                DeploymentOptions workerDeploymentOptions = new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER).setInstances(2).setWorkerPoolSize(20).setConfig(new JsonObject().put("redisHost", args[0]).put("postgresHost", args[1]));
                vertx.deployVerticle(RedisVerticle.class.getName(), workerDeploymentOptions);
                vertx.deployVerticle(PostgresVerticle.class.getName(), workerDeploymentOptions);

                LeaderBoardService leaderBoardService = new LeaderBoardServiceImpl();

                ServiceBinder binder = new ServiceBinder(vertx);
                binder.setAddress(LeaderBoardService.LEADERBOARD_SERVICE_ADDRESS).register(LeaderBoardService.class, leaderBoardService);

                ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(LeaderBoardService.LEADERBOARD_SERVICE_ADDRESS);
                DeliveryOptions deliveryOptions = new DeliveryOptions();
                deliveryOptions.setLocalOnly(true);
                builder.setOptions(deliveryOptions).build(LeaderBoardService.class);

                DeploymentOptions webDeploymentOptions = new DeploymentOptions().setThreadingModel(ThreadingModel.EVENT_LOOP).setInstances(1);
                vertx.deployVerticle(WebVerticle.class.getName(), webDeploymentOptions);
            }
        });
    }

    private static Config hazelCastConfig() {
        Properties props = System.getProperties();
        props.setProperty("vertx.hazelcast.config", "classpath:hazelcast/cluster-dev.xml");
        return ConfigUtil.loadConfig();
    }
}
