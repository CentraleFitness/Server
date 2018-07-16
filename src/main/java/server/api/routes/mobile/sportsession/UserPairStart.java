package server.api.routes.mobile.sportsession;

import Tools.LogManager;
import Tools.Token;
import com.auth0.jwt.JWT;
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

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public class UserPairStart {
    public UserPairStart(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.USER_PAIR_START.path).handler(routingContext -> {

            ResponseObject sending = null;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            label:try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rToken = (String) received.get(Protocol.Field.TOKEN.key);
                String rSessionId = (String) received.get(Protocol.Field.SESSIONID.key);
                if (rToken == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
                    break label;
                }
                if (rSessionId == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.SESSIONID.key);
                    break label;
                }
                JWT token = Token.decodeToken(rToken);
                if (token == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                    LogManager.write("Bad token");
                    break label;
                }
                User user = (User) Database.find_entity(Database.Collections.Users, User.Field.LOGIN, token.getIssuer());
                if (user == null || !rToken.equals(user.getField(User.Field.TOKEN))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                    LogManager.write("Bad token");
                    break label;
                }
                ObjectId userID = (ObjectId) user.getField(User.Field.ID);
                Module module = (Module) Database.find_entity(Database.Collections.Modules, Module.Field.SESSION_ID, rSessionId);
                if (module == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.SPORT_SESSION_BAD_SESSIONID.code);
                    LogManager.write("Bad session id");
                    break label;
                }
                ObjectId moduleID = (ObjectId) module.getField(Module.Field.ID);
                SportSession moduleSession = (SportSession) Database.find_entity(Database.Collections.SportSessions, SportSession.Field.MODULE_ID, moduleID);
                if (moduleSession != null) EndSportSession.end(moduleSession);
                SportSession userSession = (SportSession) Database.find_entity(Database.Collections.SportSessions, SportSession.Field.USER_ID, userID);
                if (userSession != null) EndSportSession.end(userSession);
                userSession = (SportSession) Database.new_entity(Database.Collections.SportSessions);
                userSession.setField(SportSession.Field.USER_ID, userID);
                userSession.setField(SportSession.Field.MODULE_ID, moduleID);
                userSession.setField(SportSession.Field.PRODUCTION, new ArrayList());
                userSession.setField(SportSession.Field.EXPIRATION, 0L);
                Database.update_entity(Database.Collections.SportSessions, userSession);
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
                LogManager.write(e);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
