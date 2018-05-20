package server.module;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import server.api.routes.module.ModuleGetIds;
import server.api.routes.module.ModulePairStop;
import server.api.routes.module.ModuleProductionSend;

/**
 * Created by hadrien on 14/03/2017.
 *
 * Tuto : http://tutorials.jenkov.com/vert.x/tcp-server.html
 */
public class ModuleVerticle extends AbstractVerticle {

    private int port;
    private HttpServer httpServer = null;
    private Router router = null;

    public ModuleVerticle(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        System.out.println("...ModuleVerticle creation... port: " + this.port);
        this.httpServer = this.vertx.createHttpServer();
        this.router = Router.router(this.vertx);
        routing();
        this.httpServer.requestHandler(this.router::accept).listen(this.port);
    }

    public void routing() {

        this.router.route().handler(CorsHandler.create("*")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type"));


        this.router.route().handler(BodyHandler.create());

        new ModuleGetIds(this.router);
        new ModuleProductionSend(this.router);
        new ModulePairStop(this.router);
    }
}
