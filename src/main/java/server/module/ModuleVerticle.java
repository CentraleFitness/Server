package server.module;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import server.api.routes.module.ModuleGetIds;

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
        System.out.println("...MobileVerticle creation...");
        this.httpServer = this.vertx.createHttpServer();
        this.router = Router.router(this.vertx);
        routing();
        this.httpServer.requestHandler(this.router::accept).listen(this.port);
    }

    public void routing() {
        this.router.route().handler(BodyHandler.create());

        new ModuleGetIds(router);
    }
}
