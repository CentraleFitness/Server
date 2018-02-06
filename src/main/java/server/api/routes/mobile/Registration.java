package server.api.routes.mobile;

import Tools.LogManager;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.User;
import protocol.mobile.Protocol;
import protocol.ResponseObject;
import Tools.PasswordAuthentication;
import Tools.Token;

import java.util.Map;

public class Registration {
    public Registration(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.REGISTRATION.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            String rLogin = (String) received.get(Protocol.Field.LOGIN.key);
            String rPassword = (String) received.get(Protocol.Field.PASSWORD.key);
            String rFirstname  = (String) received.get(Protocol.Field.FIRSTNAME.key);
            String rLasname = (String) received.get(Protocol.Field.LASTNAME.key);
            String rPhone = (String) received.get(Protocol.Field.PHONE.key);
            String rEmail = (String) received.get(Protocol.Field.EMAIL.key);

            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            Database database = Database.getInstance();

            try {
                if (rLogin == null) {
                    LogManager.write("Missing login key");
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_LOGIN.code);
                } else if (rPassword == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_PASSWORD.code);
                    LogManager.write("Missing password key");
                } else if (rFirstname == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
                    LogManager.write("Missing firstname key");
                } else if (rLasname == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
                    LogManager.write("Missing lastname key");
                } else if (rPhone == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
                    LogManager.write("Missing phone key");
                } else if (rEmail == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
                    LogManager.write("Missing email key");
                } else if (database.find_entity(Database.Collections.Users, User.Field.LOGIN, rLogin) == null) {
                    user = (User) database.new_entity(Database.Collections.Users);
                    user.setField(User.Field.LOGIN, rLogin);
                    user.setField(User.Field.PASSWORD_HASH, new PasswordAuthentication().hash((rPassword).toCharArray()));
                    user.setField(User.Field.FIRSTNAME, rFirstname);
                    user.setField(User.Field.LASTNAME, rLasname);
                    user.setField(User.Field.PHONE, rPhone);
                    user.setField(User.Field.EMAIL, rEmail);
                    user.setField(User.Field.TOKEN, new Token(rLogin, rPassword).generate());
                    database.update_entity(Database.Collections.Users, user);
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_SUCCESS.code);
                    sending.put(Protocol.Field.TOKEN.key, (String) user.getField(User.Field.TOKEN));
                } else {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_LOGIN_TAKEN.code);
                    LogManager.write("Login already taken");
                }
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_RANDOM.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
