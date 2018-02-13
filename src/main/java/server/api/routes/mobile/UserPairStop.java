package server.api.routes.mobile;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.actions.EndSportSession;
import model.entities.Module;
import model.entities.SportSession;
import model.entities.User;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public class UserPairStop {
    public UserPairStop(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.USER_PAIR_STOP.path).handler(routingContext -> {

            ResponseObject sending = null;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            label:try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rToken = (String) received.get(Protocol.Field.TOKEN.key);
                if (rToken == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
                    break label;
                }
                User user = (User) Database.find_entity(Database.Collections.Users, User.Field.LOGIN, Token.decodeToken(rToken).getIssuer());
                if (user == null || !rToken.equals(user.getField(User.Field.TOKEN))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                    LogManager.write("Bad token");
                    break label;
                }
                ObjectId userID = (ObjectId) user.getField(User.Field.ID);
                SportSession userSession = (SportSession) Database.find_entity(Database.Collections.SportSessions, SportSession.Field.USER_ID, userID);
                if (userSession != null) EndSportSession.end(userSession);
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK);
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
                LogManager.write(e);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
