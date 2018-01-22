package server.api.routes.mobile;

import Tools.PasswordAuthentication;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.User;
import protocol.Protocol;
import protocol.mobile.ResponseObject;

import java.util.Map;
import java.util.Objects;

public class UserUpdatePassword {
    public UserUpdatePassword(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.USER_UPDATE_PASSWORD.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            Database database = Database.getInstance();

            try {
                user = (User) database.find_entity(Database.Collections.Users, User.Field.LOGIN, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(user.getField(User.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else if (!new PasswordAuthentication().authenticate(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray(), (String) user.getField(User.Field.PASSWORD_HASH))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
                } else {
                    user.setField(User.Field.PASSWORD_HASH, new PasswordAuthentication().hash(((String) received.get(Protocol.Field.NEW_PASSWORD.key)).toCharArray()));
                    user.setField(User.Field.TOKEN, new Token((String) user.getField(User.Field.LOGIN), (String) received.get(Protocol.Field.NEW_PASSWORD.key)).generate());
                    database.update_entity(Database.Collections.Users, user);
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    sending.put(Protocol.Field.TOKEN.key, (String) user.getField(User.Field.TOKEN));
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

    }
}