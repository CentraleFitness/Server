package server.mobile;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import server.api.routes.mobile.*;

/**
 * Created by hadrien on 14/03/2017.
 *
 * Tuto : http://vertx.io/docs/vertx-web/java/
 */
public class MobileVerticle extends AbstractVerticle {

    private int port = 0;
    private HttpServer httpServer = null;
    private Router router = null;

    public MobileVerticle(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        System.out.println("...MobileVerticle creation... port: " + this.port);
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
        new UserGetInstantproduction(this.router);

        new UserUpdatePassword(this.router);
        new UserUpdateProfile(this.router);
        new UserUpdatePicture(this.router);

        new UserPairStart(this.router);
        new UserPairStop(this.router);
    }
}
