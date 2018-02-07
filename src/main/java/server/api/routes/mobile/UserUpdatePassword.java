package server.api.routes.mobile;

import Tools.LogManager;
import Tools.PasswordAuthentication;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.User;
import protocol.mobile.Protocol;
import protocol.ResponseObject;

import java.util.Map;
import java.util.Objects;

public class UserUpdatePassword {
    public UserUpdatePassword(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.USER_UPDATE_PASSWORD.path).handler(routingContext -> {

            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;

            try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rToken = (String) received.get(Protocol.Field.TOKEN.key);
                String rPassword = (String) received.get(Protocol.Field.PASSWORD.key);
                String rNewPassword = (String) received.get(Protocol.Field.NEW_PASSWORD.key);

                user = (User) Database.find_entity(Database.Collections.Users, User.Field.LOGIN, Token.decodeToken(rToken).getIssuer());
                if (!Objects.equals(user.getField(User.Field.TOKEN), rToken)) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                    LogManager.write("Bad token");
                } else if (!new PasswordAuthentication().authenticate((rPassword).toCharArray(), (String) user.getField(User.Field.PASSWORD_HASH))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
                    LogManager.write("Bas password");
                } else {
                    user.setField(User.Field.PASSWORD_HASH, new PasswordAuthentication().hash((rNewPassword).toCharArray()));
                    user.setField(User.Field.TOKEN, new Token((String) user.getField(User.Field.LOGIN), rNewPassword).generate());
                    Database.update_entity(Database.Collections.Users, user);
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    sending.put(Protocol.Field.TOKEN.key, (String) user.getField(User.Field.TOKEN));
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                LogManager.write(e);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

    }
}
