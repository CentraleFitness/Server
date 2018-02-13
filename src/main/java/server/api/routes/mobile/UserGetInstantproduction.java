package server.api.routes.mobile;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.SportSession;
import model.entities.User;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

import java.util.ArrayList;
import java.util.Map;

public class UserGetInstantproduction {
    public UserGetInstantproduction(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.USER_GET_INSTANTPRODUCTION.path).handler(routingContext -> {

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
                ObjectId user_id = (ObjectId) user.getField(User.Field.ID);
                SportSession sportSession = (SportSession) Database.find_entity(Database.Collections.SportSessions, SportSession.Field.USER_ID, user_id);
                if (sportSession == null) {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.NO_SPORT_SESSION.code);
                    LogManager.write("Not in sport session");
                    break label;
                }
                ArrayList production = (ArrayList) sportSession.getField(SportSession.Field.PRODUCTION);
                ArrayList emptyProd = new ArrayList();
                sportSession.setField(SportSession.Field.PRODUCTION, emptyProd);
                Database.update_entity(Database.Collections.SportSessions, sportSession);
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.PRODUCTION.key, production);
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
