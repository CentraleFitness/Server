package server.mobile;

import com.google.gson.GsonBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import model.Database;
import model.entities.User;
import org.bson.Document;
import protocol.Protocol;
import protocol.mobile.ResponseObject;
import server.misc.PasswordAuthentication;
import server.misc.Token;
import java.util.Map;
import java.util.Objects;
import static com.mongodb.client.model.Filters.eq;

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

        /**
         * Authentication with credentials
         */
        this.router.route(HttpMethod.POST, Protocol.Path.AUTHENTICATION.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            if ((user = new User((Document) this.database.users.find(eq(User.Fields.login, received.get(Protocol.Field.LOGIN.key))).first())) != null) {
                if (new PasswordAuthentication().authenticate(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray(), (String) user.get(User.Fields.passwordHash))) {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_SUCCESS.code);
                    user.put(User.Fields.token, new Token((String) received.get(Protocol.Field.LOGIN.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                    sending.put(Protocol.Field.TOKEN.key, (String) user.get(User.Fields.token));
                    this.database.users.updateOne(eq(User.Fields.login, user.get(User.Fields.login)), new Document("$set", user));
                } else {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
                }
            } else {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

        /**
         * Authentication with token
         */
        this.router.route(HttpMethod.POST, Protocol.Path.AUTHENTICATION_TOKEN.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            try {
                if ((user = new User((Document) this.database.users.find((eq(User.Fields.login, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer()))).first())) == null ||
                        !Objects.equals(user.get(User.Fields.token), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_SUCCESS.code);
                }
            }catch (NullPointerException e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

        /**
         * Registration
         */
        this.router.route(HttpMethod.POST, Protocol.Path.REGISTRATION.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            if (received.get(Protocol.Field.LOGIN.key) == null) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_LOGIN.code);
            } else if (received.get(Protocol.Field.PASSWORD.key) == null) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_PASSWORD.code);
            } else if (received.get(Protocol.Field.FIRSTNAME.key) == null) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
            } else if (received.get(Protocol.Field.LASTNAME.key) == null) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
            } else if (received.get(Protocol.Field.PHONE.key) == null) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
            } else if (received.get(Protocol.Field.EMAIL.key) == null) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
            } else if (new User((Document) this.database.users.find(eq(User.Fields.login, received.get(Protocol.Field.LOGIN.key))).first()) == null) {
                user = new User();
                user.put(User.Fields.login, received.get(Protocol.Field.LOGIN.key));
                user.put(User.Fields.passwordHash, new PasswordAuthentication().hash(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray()));
                user.put(User.Fields.firstName, received.get(Protocol.Field.FIRSTNAME.key));
                user.put(User.Fields.lastName, received.get(Protocol.Field.LASTNAME.key));
                user.put(User.Fields.phone, received.get(Protocol.Field.PHONE.key));
                user.put(User.Fields.email, received.get(Protocol.Field.EMAIL.key));
                user.put(User.Fields.token, new Token((String) received.get(Protocol.Field.LOGIN.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                this.database.users.insertOne(user);
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_SUCCESS.code);
                sending.put(Protocol.Field.TOKEN.key, (String) user.get(User.Fields.token));
            } else {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_LOGIN_TAKEN.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

        /**
         * User Profile
         */
        this.router.route(HttpMethod.POST, Protocol.Path.USERPROFILE.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            try {
                if ((user = new User((Document) this.database.users.find((eq(User.Fields.login, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer()))).first())) == null ||
                        !Objects.equals(user.get(User.Fields.token), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    sending.put(User.Fields.login, (String) user.get(User.Fields.login));
                    sending.put(User.Fields.firstName, (String) user.get(User.Fields.firstName));
                    sending.put(User.Fields.lastName, (String) user.get(User.Fields.lastName));
                    sending.put(User.Fields.email, (String) user.get(User.Fields.email));
                    sending.put(User.Fields.phone, (String) user.get(User.Fields.phone));
                }
            }catch (NullPointerException e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

        /**
         * Production instantannée
         */
        this.router.route(HttpMethod.POST, Protocol.Path.USERWATTPRODUCTIONINSTANT.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            model.entities.Module module;
            try {
                if ((user = new User((Document) this.database.users.find((eq(User.Fields.login, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer()))).first())) == null ||
                        !Objects.equals(user.get(User.Fields.token), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    if ((module = new model.entities.Module((Document) this.database.modules.find(eq(model.entities.Module.Fields.currentUser, user.get(User.Fields.login))).first())) == null) {
                        sending.put(Protocol.Field.INSTANTWATT.key, "0.0");
                        sending.put(Protocol.Field.MODULENAME.key, "null");
                        sending.put(Protocol.Field.MACHINETYPE.key, "null");
                    } else {
                        sending.put(Protocol.Field.INSTANTWATT.key, String.valueOf(module.getWattProductionInstant()));
                        sending.put(Protocol.Field.MODULENAME.key, module.getName());
                        sending.put(Protocol.Field.MACHINETYPE.key, module.getMachineType());
                    }
                }
            }catch (NullPointerException e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }

    public void setDatabase(Database database) {
        this.database = database;
    }
}
