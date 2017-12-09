package server.api.routes.mobile;

import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.User;
import protocol.Protocol;
import protocol.mobile.ResponseObject;
import server.misc.PasswordAuthentication;
import server.misc.Token;

import java.util.Map;

public class Authentication_with_credentials {
    public Authentication_with_credentials(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.AUTHENTICATION.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            Database database = Database.getInstance();
            try {
                if ((user = (User) database.find_entity(Database.Collections.Users, User.Field.LOGIN, received.get(Protocol.Field.LOGIN.key))) != null) {
                    if (new PasswordAuthentication().authenticate(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray(), (String) user.getField(User.Field.PASSWORD_HASH))) {
                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_SUCCESS.code);
                        user.setField(User.Field.TOKEN, new Token((String) received.get(Protocol.Field.LOGIN.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                        sending.put(Protocol.Field.TOKEN.key, (String) user.getField(User.Field.TOKEN));
                        database.update_entity(Database.Collections.Users, user);
                    } else {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
                    }
                } else {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
                }
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_CREDENTIALS.code);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
