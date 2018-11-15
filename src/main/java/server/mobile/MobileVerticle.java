package server.mobile;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import server.api.routes.mobile.challenge.*;
import server.api.routes.mobile.customprogram.CustomProgramCreate;
import server.api.routes.mobile.customprogram.CustomProgramDelete;
import server.api.routes.mobile.customprogram.CustomProgramGetPreview;
import server.api.routes.mobile.customprogram.CustomProgramGetRange;
import server.api.routes.mobile.customprogram.CustomProgramGetSteps;
import server.api.routes.mobile.customprogram.favorites.CustomProgramFavAdd;
import server.api.routes.mobile.customprogram.favorites.CustomProgramFavGetRange;
import server.api.routes.mobile.customprogram.favorites.CustomProgramFavRemove;
import server.api.routes.mobile.event.EventRegistration;
import server.api.routes.mobile.event.GetEventPreview;
import server.api.routes.mobile.event.GetEventUsers;
import server.api.routes.mobile.event.GetEvents;
import server.api.routes.mobile.post.GetPostContent;
import server.api.routes.mobile.post.GetPosts;
import server.api.routes.mobile.post.PostCreate;
import server.api.routes.mobile.post.PostDelete;
import server.api.routes.mobile.post.PostGetLikes;
import server.api.routes.mobile.post.PostLike;
import server.api.routes.mobile.post.comment.PostCommentCreate;
import server.api.routes.mobile.post.comment.PostCommentDelete;
import server.api.routes.mobile.post.comment.PostCommentGetRange;
import server.api.routes.mobile.sportsession.GetSportSession;
import server.api.routes.mobile.sportsession.GetSportSessionStats;
import server.api.routes.mobile.sportsession.GetSportSessions;
import server.api.routes.mobile.sportsession.UserGetInstantproduction;
import server.api.routes.mobile.sportsession.UserGetTotalproduction;
import server.api.routes.mobile.sportsession.UserPairStart;
import server.api.routes.mobile.sportsession.UserPairStop;
import server.api.routes.mobile.user.auth.AuthenticationWithCredentials;
import server.api.routes.mobile.user.auth.AuthenticationWithToken;
import server.api.routes.mobile.user.auth.Registration;
import server.api.routes.mobile.user.profile.Affiliate;
import server.api.routes.mobile.user.profile.GetAffiliation;
import server.api.routes.mobile.user.profile.UnAffiliate;
import server.api.routes.mobile.user.profile.UserGetPicture;
import server.api.routes.mobile.user.profile.UserGetProfile;
import server.api.routes.mobile.user.profile.UserUpdatePassword;
import server.api.routes.mobile.user.profile.UserUpdatePicture;
import server.api.routes.mobile.user.profile.UserUpdateProfile;

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


        this.router.route().handler(CorsHandler.create("*")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type"));


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
        new EventRegistration(router);     
        new Affiliate(router);
        new GetAffiliation(router);
        new GetPosts(router);
        new GetPostContent(router);
        new GetSportSessions(router);
        new GetSportSession(router);
        new GetSportSessionStats(router); 
        new PostCreate(router);
        new PostDelete(router);
        new PostLike(router);
        new PostGetLikes(router);
        new PostCommentCreate(router);
        new PostCommentDelete(router);
        new PostCommentGetRange(router);
        new ChallengeCreate(router);
        new ChallengeInvite(router);
        new ChallengeInviteAnswer(router);
        new ChallengeGetRange(router);
        new ChallengeComplete(router);
        new ChallengeQuit(router);
        new CustomProgramCreate(router);
        new CustomProgramDelete(router);
        new CustomProgramGetRange(router);
        new CustomProgramGetPreview(router);
        new CustomProgramGetSteps(router);
        new CustomProgramFavAdd(router);
        new CustomProgramFavRemove(router);
        new CustomProgramFavGetRange(router);
        new ChallengeInit(router);
        new UnAffiliate(router);
        new UserGetTotalproduction(router);
    }
}
