package server.image;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import server.api.routes.image.Delete;
import server.api.routes.image.GenerateTemporaryURL;
import server.api.routes.image.Get;
import server.api.routes.image.Store;

import java.io.File;
import java.util.HashMap;

public class ImageVerticle extends AbstractVerticle {

    private int mPort;
    private HttpServer mHttpServer = null;
    private Router mRouter = null;
    private String mRoot = "./Pictures";
    private String mDBIP = "localhost";
    private int mDBPort = 27017;
    private String mDBName = "ImageVerticle";
    private MongoClient mDBClient;
    private MongoDatabase mDB;
    private MongoCollection mDBCUrls;

    public ImageVerticle(int port) {
        mPort = port;
        mDBClient = new MongoClient(mDBIP, mPort);
        mDB = mDBClient.getDatabase(mDBName);
        mDBCUrls = mDB.getCollection("urls");

        File theDir = new File(mRoot);
        if (!theDir.exists()) theDir.mkdir();
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

    public MongoCollection getDBCUrls() {
        return mDBCUrls;
    }

    @Deprecated
    public String getToken() {
        return "toto";
    }
}
