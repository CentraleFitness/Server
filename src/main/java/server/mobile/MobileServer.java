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

import static com.mongodb.client.model.Filters.and;
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
            Database.User user;
            if ((user = new Database.User((Document) this.database.users.find(eq(Database.User.Fields.login, received.get(Protocol.Field.LOGIN.key))).first())).getDoc() != null) {
                if (new PasswordAuthentication().authenticate(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray(), user.getPasswordHash())) {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_SUCCESS.code);
                    user.setToken(new Token((String) received.get(Protocol.Field.LOGIN.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                    sending.put(Protocol.Field.TOKEN.key, user.getToken());
                    this.database.users.updateOne(eq(Database.User.Fields.login, user.getLogin()), new Document("$set", user.getDoc()));
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
            Database.User user;
            try {
                if ((user = new Database.User((Document) this.database.users.find((eq(Database.User.Fields.login, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer()))).first())).getDoc() == null ||
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
            } else if (new Database.User((Document) this.database.users.find(eq(Database.User.Fields.login, received.get(Protocol.Field.LOGIN.key))).first()).getDoc() == null) {
                user = new Database.User();
                user.setLogin((String) received.get(Protocol.Field.LOGIN.key));
                user.setPasswordHash(new PasswordAuthentication().hash(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray()));
                user.setFirstName((String) received.get(Protocol.Field.FIRSTNAME.key));
                user.setLastName((String) received.get(Protocol.Field.LASTNAME.key));
                user.setPhoneNumber((String) received.get(Protocol.Field.PHONE.key));
                user.setEmailAddress((String) received.get(Protocol.Field.EMAIL.key));
                user.setToken(new Token((String) received.get(Protocol.Field.LOGIN.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                this.database.users.insertOne(user.getDoc());
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_SUCCESS.code);
                sending.put(Protocol.Field.TOKEN.key, user.getToken());
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
            Database.User user;
            try {
                if ((user = new Database.User((Document) this.database.users.find((eq(Database.User.Fields.login, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer()))).first())).getDoc() == null ||
                        !Objects.equals(user.getToken(), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    sending.put(Database.User.Fields.login, user.getLogin());
                    sending.put(Database.User.Fields.firstName, user.getFirstName());
                    sending.put(Database.User.Fields.lastName, user.getLastName());
                    sending.put(Database.User.Fields.email, user.getEmailAddress());
                    sending.put(Database.User.Fields.phone, user.getPhoneNumber());
                }
            }catch (NullPointerException e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

        this.router.route(HttpMethod.POST, Protocol.Path.USERWATTPRODUCTIONINSTANT.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Database.User user;
            Database.Module module;
            try {
                if ((user = new Database.User((Document) this.database.users.find((eq(Database.User.Fields.login, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer()))).first())).getDoc() == null ||
                        !Objects.equals(user.getToken(), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    if ((module = new Database.Module((Document) this.database.modules.find(eq(Database.Module.Fields.currentUser, user.getLogin())).first())) == null) {
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

        /**
         * Tricks forward
         */
        this.router.route(HttpMethod.POST, "/triX").handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            sending = new ResponseObject(false);

            String moduleName = "module1";

            Database.Module module = new Database.Module((Document) this.database.modules.find(eq(Database.Module.Fields.moduleName, moduleName)).first());
            Database.User user = new Database.User((Document) this.database.users.find(eq(Database.User.Fields.login, module.getCurrentUser())).first());
            Database.ElectricProduction electricProduction = new Database.ElectricProduction((Document) this.database.electricProductions.find(and(eq(Database.ElectricProduction.Fields.userId, user.getDoc().get("_id")), eq(Database.ElectricProduction.Fields.moduleId, module.getDoc().get("_id")))).first());

            double watt = Double.valueOf((String) received.get("trix"));
            user.setWattProduction_day(watt + user.getWattProductionDay());
            module.setWattProduction_day(watt + module.getWattProductionDay());
            module.setWattProduction_instant(watt);
            electricProduction.setWattProduction_day(watt + electricProduction.getWattProductionDay());
            this.database.users.updateOne(eq(Database.idKey, user.getId()), user.getUpdate());
            this.database.modules.updateOne(eq(Database.idKey, module.getId()), module.getUpdate());
            this.database.electricProductions.updateOne(eq(Database.idKey, electricProduction.getId()), electricProduction.getUpdate());

            sending.put("trix", String.valueOf(watt));
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }

    public void setDatabase(Database database) {
        this.database = database;
    }
}
