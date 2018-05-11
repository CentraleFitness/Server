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
        router.route().handler(BodyHandler.create());

        new Registration(router);

        new AuthenticationWithCredentials(router);
        new AuthenticationWithToken(router);

        new UserGetProfile(router);
        new UserGetPicture(router);
        new UserGetInstantproduction(router);

        new UserUpdatePassword(router);
        new UserUpdateProfile(router);
        new UserUpdatePicture(router);

        new UserPairStart(router);
        new UserPairStop(router);

        new GetEvents(router);
        new GetEventPreview(router);
        new GetEventUsers(router);
        
        new Affiliate(router);
        new GetAffiliation(router);
        
        new GetPosts(router);
        new GetPostContent(router);
    }
}
