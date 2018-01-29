package server.api.routes.mobile;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.User;
import protocol.mobile.Protocol;
import protocol.mobile.ResponseObject;

import java.util.Map;
import java.util.Objects;

public class UserGetProfile {
    public UserGetProfile(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.USER_GET_PROFILE.path).handler(routingContext -> {
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
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                    sending.put(Protocol.Field.LOGIN.key, (String) user.getField(User.Field.LOGIN));
                    sending.put(Protocol.Field.FIRSTNAME.key, (String) user.getField(User.Field.FIRSTNAME));
                    sending.put(Protocol.Field.LASTNAME.key, (String) user.getField(User.Field.LASTNAME));
                    sending.put(Protocol.Field.EMAIL.key, (String) user.getField(User.Field.EMAIL));
                    sending.put(Protocol.Field.PHONE.key, (String) user.getField(User.Field.PHONE));
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
