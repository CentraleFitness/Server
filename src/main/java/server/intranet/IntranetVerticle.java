package server.intranet;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import model.Database;

public class IntranetVerticle extends AbstractVerticle {


    private int port = 0;
    private HttpServer httpServer = null;
    private Router router = null;
    private Database database = null;

    public IntranetVerticle(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        System.out.println("...Intranet creation...");
        this.httpServer = this.vertx.createHttpServer();
        this.router = Router.router(this.vertx);
        routing();
        this.httpServer.requestHandler(this.router::accept).listen(this.port);
    }

    public void routing() {
        this.router.route().handler(BodyHandler.create());


    }


    public void setDatabase(Database database){
        this.database = database;
    }

}
