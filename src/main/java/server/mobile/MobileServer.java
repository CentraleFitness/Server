package server.mobile;

import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import model.Database;
import model.entities.Picture;
import model.entities.User;
import model.entities._IDS_;
import org.bson.Document;
import protocol.Protocol;
import protocol.mobile.ResponseObject;
import server.misc.PasswordAuthentication;
import server.misc.Token;

import java.math.BigInteger;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;

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
            MongoCollection users = this.database.collections.get(Database.Collections.Users);
            try {
                if ((user = (User) this.database.find_entity(Database.Collections.Users, User.Field.LOGIN, received.get(Protocol.Field.LOGIN.key))) != null) {
                    if (new PasswordAuthentication().authenticate(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray(), (String) user.getField(User.Field.PASSWORD_HASH))) {
                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_SUCCESS.code);
                        user.setField(User.Field.TOKEN, new Token((String) received.get(Protocol.Field.LOGIN.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                        sending.put(Protocol.Field.TOKEN.key, (String) user.getField(User.Field.TOKEN));
                        this.database.update_entity(Database.Collections.Users, user);
                    } else {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
                    }
                } else {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
                }
            } catch (NullPointerException e) {
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
                user = (User) this.database.find_entity(Database.Collections.Users, User.Field.LOGIN, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(user.getField(User.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
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
            MongoCollection users = this.database.collections.get(Database.Collections.Users);

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
            } else if (this.database.find_entity(Database.Collections.Users, User.Field.LOGIN, received.get(Protocol.Field.LOGIN.key)) == null) {
                user = (User) this.database.new_entity(Database.Collections.Users);
                user.setField(User.Field.LOGIN, received.get(Protocol.Field.LOGIN.key));
                user.setField(User.Field.PASSWORD_HASH, new PasswordAuthentication().hash(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray()));
                user.setField(User.Field.FIRSTNAME, received.get(Protocol.Field.FIRSTNAME.key));
                user.setField(User.Field.LASTNAME, received.get(Protocol.Field.LASTNAME.key));
                user.setField(User.Field.PHONE, received.get(Protocol.Field.PHONE.key));
                user.setField(User.Field.EMAIL, received.get(Protocol.Field.EMAIL.key));
                user.setField(User.Field.TOKEN, new Token((String) received.get(Protocol.Field.LOGIN.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                this.database.update_entity(Database.Collections.Users, user);
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_SUCCESS.code);
                sending.put(Protocol.Field.TOKEN.key, (String) user.getField(User.Field.TOKEN));
            } else {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_LOGIN_TAKEN.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

        /**
         * User Get Profile
         */
        this.router.route(HttpMethod.POST, Protocol.Path.USER_GET_PROFILE.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            try {
                user = (User) this.database.find_entity(Database.Collections.Users, User.Field.LOGIN, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(user.getField(User.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    sending.put(Protocol.Field.LOGIN.key, (String) user.getField(User.Field.LOGIN));
                    sending.put(Protocol.Field.FIRSTNAME.key, (String) user.getField(User.Field.FIRSTNAME));
                    sending.put(Protocol.Field.LASTNAME.key, (String) user.getField(User.Field.LASTNAME));
                    sending.put(Protocol.Field.EMAIL.key, (String) user.getField(User.Field.EMAIL));
                    sending.put(Protocol.Field.PHONE.key, (String) user.getField(User.Field.PHONE));
                }
            }catch (NullPointerException e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

        /**
         * User Get Picture
         */
        this.router.route(HttpMethod.POST, Protocol.Path.USER_GET_PICTURE.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            Picture picture;
            try {
                user = (User) this.database.find_entity(Database.Collections.Users, User.Field.LOGIN, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                picture = (Picture) this.database.find_entity(Database.Collections.Pictures, Picture.Field.PICTURE_ID, user.getField(User.Field.PICTURE_ID));
                if (!Objects.equals(user.getField(User.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    sending.put(Protocol.Field.PICTURE.key, picture.getField(Picture.Field.PICTURE).toString());
                }
            }catch (NullPointerException e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

        /**
         * User Password Update
         */
        this.router.route(HttpMethod.POST, Protocol.Path.USER_UPDATE_PASSWORD.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            try {
                user = (User) this.database.find_entity(Database.Collections.Users, User.Field.LOGIN, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(user.getField(User.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    this.database.update_entity(Database.Collections.Users, user);
                }
            }catch (NullPointerException e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

        /**
         * User Profile Update
         */
        this.router.route(HttpMethod.POST, Protocol.Path.USER_UPDATE_PROFILE.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            try {
                user = (User) this.database.find_entity(Database.Collections.Users, User.Field.LOGIN, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(user.getField(User.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    user.setField(User.Field.FIRSTNAME, received.get(Protocol.Field.FIRSTNAME));
                    user.setField(User.Field.LASTNAME, received.get(Protocol.Field.LASTNAME));
                    user.setField(User.Field.EMAIL, received.get(Protocol.Field.EMAIL));
                    user.setField(User.Field.PHONE, received.get(Protocol.Field.PHONE));
                    this.database.update_entity(Database.Collections.Users, user);
                }
            }catch (NullPointerException e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

        /**
         * User Picture Update
         */
        this.router.route(HttpMethod.POST, Protocol.Path.USER_UPDATE_PICTURE.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            Base64 pic64;
            try {
                user = (User) this.database.find_entity(Database.Collections.Users, User.Field.LOGIN, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(user.getField(User.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else if ((pic64 = (Base64) received.get(Protocol.Field.PICTURE)) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
                }
                else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    Picture pic = (Picture) this.database.new_entity(Database.Collections.Pictures);
                    pic.setField(Picture.Field.PICTURE, pic64);
                    user.setField(User.Field.PICTURE_ID, pic.getField(Picture.Field.PICTURE_ID));
                    this.database.update_entity(Database.Collections.Pictures, pic);
                    this.database.update_entity(Database.Collections.Users, user);
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
