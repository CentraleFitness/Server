package server.image;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import server.api.routes.image.Delete;
import server.api.routes.image.GenerateTemporaryURL;
import server.api.routes.image.Get;
import server.api.routes.image.Store;

public class ImageVerticle extends AbstractVerticle {

    private int mPort;
    private HttpServer mHttpServer = null;
    private Router mRouter = null;
    private String mRoot = "./";

    public ImageVerticle(int port) {
        mPort = port;
    }

    @Override
    public void start() {
        System.out.println("...ImageVerticle creation... port: " + mPort);
        mHttpServer = this.vertx.createHttpServer();
        mRouter = Router.router(this.vertx);
        routing();
        mHttpServer.requestHandler(mRouter::accept).listen(mPort);
    }

    public void routing() {
        mRouter.route().handler(BodyHandler.create());

        new Store(mRouter, this);
        new Get(mRouter, this);
        new Delete(mRouter, this);
        new GenerateTemporaryURL(mRouter, this);
    }

    public String getRoot() {
        return mRoot;
    }
}
