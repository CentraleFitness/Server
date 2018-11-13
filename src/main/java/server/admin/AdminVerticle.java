package server.admin;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import model.Database;
import server.api.routes.admin.*;

public class AdminVerticle extends AbstractVerticle {


    private int port = 0;
    private HttpServer httpServer = null;
    private Router router = null;
    private Database database = null;

    public AdminVerticle(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        System.out.print("...Admin creation... port : ");
        System.out.println(this.port);
        this.httpServer = this.vertx.createHttpServer();
        this.router = Router.router(this.vertx);
        routing();
        this.httpServer.requestHandler(this.router::accept).listen(this.port);
    }

    public void routing() {


        this.router.route().handler(CorsHandler.create("*")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
                .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type"));
        this.router.route().handler(BodyHandler.create());

        new AuthenticationWithCredentials(this.router);
        new AuthenticationWithToken(this.router);

        new CreateAccount(this.router);
        new GetAccounts(this.router);
        new UpdateAccount(this.router);
        new UpdatePassword(this.router);
        new DeleteAccount(this.router);

        new GetFitnessCenters(this.router);

        new GetManagers(this.router);
        new ValidateManager(this.router);
        new SetManagerAccountActivity(this.router);
        new UndoRefuseManager(this.router);

        new ConsultSiretApi(this.router);

        new GetMobileFeedbacks(this.router);
        new GetManagerFeedbacks(this.router);
        new UpdateManagerFeeback(this.router);

        new GetModules(this.router);
        new CreateModule(this.router);
        new UpdateModule(this.router);
        new GetModuleStates(this.router);


    }


    public void setDatabase(Database database){
        this.database = database;
    }

}
