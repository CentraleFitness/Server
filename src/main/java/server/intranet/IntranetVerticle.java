package server.intranet;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import model.Database;
import server.api.routes.intranet.account.*;
import server.api.routes.intranet.customprogram.*;
import server.api.routes.intranet.displayconfiguration.*;
import server.api.routes.intranet.event.*;
import server.api.routes.intranet.feedback.*;
import server.api.routes.intranet.homesummary.*;
import server.api.routes.intranet.manager.*;
import server.api.routes.intranet.module.*;
import server.api.routes.intranet.picture.*;
import server.api.routes.intranet.publication.*;
import server.api.routes.intranet.statistic.*;

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


        this.router.route().handler(CorsHandler.create("*")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type"));
        this.router.route().handler(BodyHandler.create());

        new RegisterManagerAndCenter(this.router);

        new RegisterManager(this.router);

        new AuthenticationWithCredentials(this.router);
        new AuthenticationWithToken(this.router);

        new ManagerUpdatePassword(this.router);
        new ManagerGetProfile(this.router);
        new ManagerUpdateProfile(this.router);
        new ManagerUpdatePicture(this.router);
        new ManagerGetPicture(this.router);

        new ValidateManager(this.router);
        new SetManagerAccountActivity(this.router);

        new CenterGetProfile(this.router);
        new CenterUpdateProfile(this.router);
        new CenterGetPicture(this.router);
        new CenterUpdatePicture(this.router);

        new CenterAddPictureToAlbum(this.router);
        new CenterDeletePictureFromAlbum(this.router);
        new CenterGetAlbum(this.router);

        new CenterAddPublication(this.router);
        new CenterLikePublication(this.router);
        new CenterReportPublication(this.router);
        new CenterDeletePublication(this.router);
        new CenterGetPublications(this.router);

        new GetFeedbackStates(this.router);
        new AddFeedback(this.router);
        new AddResponseFeedback(this.router);
        new GetFeedbacks(this.router);

        new AddEvent(this.router);
        new GetEvents(this.router);
        new UpdateEvent(this.router);
        new DeleteEvent(this.router);
        new PostEvent(this.router);

        new GetHomeSummary(this.router);

        new GetDisplayConfiguration(this.router);
        new UpdateDisplayConfiguration(this.router);

        new GetModuleStates(this.router);
        new GetModules(this.router);
        new SetModuleReceived(this.router);

        new GetActivities(this.router);
        new AddCustomProgram(this.router);
        new UpdateCustomProgram(this.router);
        new SetCustomProgramsAvailability(this.router);
        new GetCustomPrograms(this.router);
        new DeleteCustomProgram(this.router);

        new GetStatistics(this.router);

        new GetFitnessCenterId(this.router);

        new GetSecondaryManagers(this.router);

        new GetCenterId(this.router);
        new GetDisplayBestCenterProdYear(this.router);
        new GetDisplayBestUserProdDay(this.router);
        new GetDisplayBestUserProdYear(this.router);
        new GetDisplayConfig(this.router);
        new GetDisplayEvent(this.router);
        new GetDisplayProductionPerModule(this.router);
        new GetDisplayUser(this.router);
        new GetDisplayUserPicture(this.router);

    }


    public void setDatabase(Database database){
        this.database = database;
    }

}
