package server.image;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import server.api.routes.image.Delete;
import server.api.routes.image.GenerateTemporaryURL;
import server.api.routes.image.Get;
import server.api.routes.image.Store;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class ImageVerticle extends AbstractVerticle {

    private int mPort;
    private HttpServer mHttpServer = null;
    private Router mRouter = null;
    private String mRoot = "./Pictures";
    private Cache<String, String> mUrls = null;

    public ImageVerticle(int port) {
        mPort = port;
       File theDir = new File(mRoot);
        if (!theDir.exists()) theDir.mkdir();
        mUrls = CacheBuilder.newBuilder()
                .maximumSize(10000) // Taille Max
                .expireAfterWrite(5, TimeUnit.SECONDS) // TTL
                .build();
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

        new Store(this);
        new Get(this);
        new Delete(this);
        new GenerateTemporaryURL(this);
    }

    public String getRoot() {
        return mRoot;
    }

    public Router getRouter() {
        return mRouter;
    }

    public Cache<String, String> getUrls() {
        return mUrls;
    }

    @Deprecated
    public String getToken() {
        return "toto";
    }
}
