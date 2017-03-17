package server.mobile;

import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import model.Database;
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
    private MongoDatabase database = null;

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
            MongoCollection users = this.database.getCollection(Database.Collections.Users.key);
            Database.User user;
            if ((user = new Database.User((Document) users.find(eq(Database.Collections.Users.Field.login, received.get(Protocol.Field.LOGIN.key))).first())).getDoc() != null) {
                if (new PasswordAuthentication().authenticate(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray(), user.getPasswordHash())) {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_SUCCESS.code);
                    user.setToken(new Token((String) received.get(Protocol.Field.LOGIN.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                    sending.put(Protocol.Field.TOKEN.key, user.getToken());
                    users.updateOne(eq(Database.Collections.Users.Field.login, user.getLogin()), new Document("$set", user.getDoc()));
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
            MongoCollection users = this.database.getCollection(Database.Collections.Users.key);
            Database.User user;
            try {
                if ((user = new Database.User((Document) users.find((eq(Database.Collections.Users.Field.login, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer()))).first())).getDoc() == null ||
                        !Objects.equals(user.getToken(), received.get(Protocol.Field.TOKEN.key))) {
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
            MongoCollection users = this.database.getCollection(Database.Collections.Users.key);
            Database.User user;
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
            } else if (new Database.User((Document) users.find(eq(Database.Collections.Users.Field.login, received.get(Protocol.Field.LOGIN.key))).first()).getDoc() == null) {
                user = new Database.User();
                user.setLogin((String) received.get(Protocol.Field.LOGIN.key));
                user.setPasswordHash(new PasswordAuthentication().hash(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray()));
                user.setFirstName((String) received.get(Protocol.Field.FIRSTNAME.key));
                user.setLastName((String) received.get(Protocol.Field.LASTNAME.key));
                user.setPhoneNumber((String) received.get(Protocol.Field.PHONE.key));
                user.setEmailAddress((String) received.get(Protocol.Field.EMAIL.key));
                user.setToken(new Token((String) received.get(Protocol.Field.LOGIN.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                users.insertOne(user.getDoc());
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_SUCCESS.code);
                sending.put(Protocol.Field.TOKEN.key, user.getToken());
            } else {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_LOGIN_TAKEN.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }
}
