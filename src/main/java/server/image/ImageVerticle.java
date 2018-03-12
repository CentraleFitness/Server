package server.image;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class ImageVerticle extends AbstractVerticle {

    private int port;
    private HttpServer httpServer = null;
    private Router router = null;

    public ImageVerticle(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        System.out.println("...ImageVerticle creation... port: " + this.port);
        this.httpServer = this.vertx.createHttpServer();
        this.router = Router.router(this.vertx);
        routing();
        this.httpServer.requestHandler(this.router::accept).listen(this.port);
    }

    public void routing() {
        this.router.route().handler(BodyHandler.create());
    }
}
