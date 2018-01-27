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
import Tools.Token;

import java.util.Map;
import java.util.Objects;

public class AuthenticationWithToken {
    public AuthenticationWithToken(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.AUTHENTICATION_TOKEN.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            String rToken = (String) received.get(Protocol.Field.TOKEN.key);

            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            User user;
            Database database = Database.getInstance();

            try {
                user = (User) database.find_entity(Database.Collections.Users, User.Field.LOGIN, Token.decodeToken(rToken).getIssuer());
                if (!Objects.equals(user.getField(User.Field.TOKEN), rToken)) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                    LogManager.write("Bad token");
                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_SUCCESS.code);
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
