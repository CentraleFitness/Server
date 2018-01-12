package server.api.routes.mobile;

import Tools.LogManager;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.User;
import protocol.Protocol;
import protocol.mobile.ResponseObject;
import Tools.PasswordAuthentication;
import Tools.Token;

import java.util.Map;

public class Registration {
    public Registration(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.REGISTRATION.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            Database database = Database.getInstance();
            LogManager log = LogManager.getINSTANCE();

            try {
                if (received.get(Protocol.Field.LOGIN.key) == null) {
                    log.write(this, "Missing login key");
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_LOGIN.code);
                } else if (received.get(Protocol.Field.PASSWORD.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_PASSWORD.code);
                    log.write(this, "Missing password key");
                } else if (received.get(Protocol.Field.FIRSTNAME.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
                    log.write(this, "Missing firstname key");
                } else if (received.get(Protocol.Field.LASTNAME.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
                    log.write(this, "Missing lastname key");
                } else if (received.get(Protocol.Field.PHONE.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
                    log.write(this, "Missing phone key");
                } else if (received.get(Protocol.Field.EMAIL.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
                    log.write(this, "Missing email key");
                } else if (database.find_entity(Database.Collections.Users, User.Field.LOGIN, received.get(Protocol.Field.LOGIN.key)) == null) {
                    user = (User) database.new_entity(Database.Collections.Users);
                    user.setField(User.Field.LOGIN, received.get(Protocol.Field.LOGIN.key));
                    user.setField(User.Field.PASSWORD_HASH, new PasswordAuthentication().hash(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray()));
                    user.setField(User.Field.FIRSTNAME, received.get(Protocol.Field.FIRSTNAME.key));
                    user.setField(User.Field.LASTNAME, received.get(Protocol.Field.LASTNAME.key));
                    user.setField(User.Field.PHONE, received.get(Protocol.Field.PHONE.key));
                    user.setField(User.Field.EMAIL, received.get(Protocol.Field.EMAIL.key));
                    user.setField(User.Field.TOKEN, new Token((String) received.get(Protocol.Field.LOGIN.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                    database.update_entity(Database.Collections.Users, user);
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_SUCCESS.code);
                    sending.put(Protocol.Field.TOKEN.key, (String) user.getField(User.Field.TOKEN));
                } else {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_LOGIN_TAKEN.code);
                    log.write(this, "Login already taken");
                }
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
                log.write(this, "Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
