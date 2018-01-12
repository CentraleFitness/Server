package server.mobile;

import com.google.gson.GsonBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import model.Database;
import model.entities.Picture;
import model.entities.User;
import protocol.Protocol;
import protocol.mobile.ResponseObject;
import server.api.routes.mobile.*;
import Tools.Token;

import java.util.Map;
import java.util.Objects;

/**
 * Created by hadrien on 14/03/2017.
 *
 * Tuto : http://vertx.io/docs/vertx-web/java/
 */
public class MobileServer extends AbstractVerticle {

    private int port = 0;
    private HttpServer httpServer = null;
    private Router router = null;
    private Database database = null;

    public MobileServer(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        System.out.println("...MobileServer creation...");
        this.httpServer = this.vertx.createHttpServer();
        this.router = Router.router(this.vertx);
        routing();
        this.httpServer.requestHandler(this.router::accept).listen(this.port);
    }

    public void routing() {
        this.router.route().handler(BodyHandler.create());

        new Registration(this.router);

        new AuthenticationWithCredentials(this.router);
        new AuthenticationWithToken(this.router);

        new UserGetProfile(this.router);
        new UserGetPicture(this.router);
        new UserUpdatePassword(this.router);
        new UserUpdateProfile(this.router);
        new UserUpdatePicture(this.router);
    }

    public void setDatabase(Database database) {
        this.database = database;
    }
}
