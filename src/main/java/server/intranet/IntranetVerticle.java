package server.intranet;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import model.Database;
import server.api.routes.intranet.*;

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
        System.out.print("...Intranet creation... port : ");
        System.out.println(this.port);
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

        new ManagerUpdatePassword(this.router);
        new ManagerGetProfile(this.router);
        new ManagerUpdateProfile(this.router);
        new ManagerUpdatePicture(this.router);
        new ManagerGetPicture(this.router);

        new CenterRegistration(this.router);
        new CenterGetProfile(this.router);
        new CenterUpdateProfile(this.router);
        new CenterGetPicture(this.router);
        new CenterUpdatePicture(this.router);

        new CenterAddPictureToAlbum(this.router);
        new CenterDeletePictureFromAlbum(this.router);
        new CenterGetAlbum(this.router);

        new CenterAddPublication(this.router);
        new CenterDeletePublication(this.router);
        new CenterGetPublications(this.router);
    }


    public void setDatabase(Database database){
        this.database = database;
    }

}
