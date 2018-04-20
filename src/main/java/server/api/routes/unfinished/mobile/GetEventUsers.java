package server.api.routes.unfinished.mobile;

import Tools.LogManager;
import Tools.Token;
import com.auth0.jwt.JWT;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Event;
import model.entities.TUPLE_Event_User;
import model.entities.User;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GetEventUsers {
    public GetEventUsers(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.GET_EVENTSUSERS.path).handler(routingContext -> {

            ResponseObject sending = null;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            label:try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rToken = (String) received.get(Protocol.Field.TOKEN.key);
                String rEventId = (String) received.get(Protocol.Field.EVENTID.key);
                if (rToken == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
                    break label;
                }
                if (rEventId == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.EVENTID.key);
                    break label;
                }
                JWT token = Token.decodeToken(rToken);
                if (token == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                    LogManager.write(Protocol.Status.AUTH_ERROR_TOKEN.message);
                    break label;
                }
                User user = (User) Database.find_entity(Database.Collections.Users, User.Field.LOGIN, token.getIssuer());
                if (user == null || !rToken.equals(user.getField(User.Field.TOKEN))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                    LogManager.write(Protocol.Status.AUTH_ERROR_TOKEN.message);
                    break label;
                }
                ObjectId eventId = new ObjectId(rEventId);
                List<String> users =
                        Database
                        .find_entities(Database.Collections.TUPLE_Event_Users, TUPLE_Event_User.Field.EVENT_ID, eventId)
                        .stream()
                        .map(tuple -> {
                                    try {
                                        return (String)
                                                Database.find_entity(
                                                Database.Collections.Users,
                                                User.Field.ID,
                                                tuple.getField(TUPLE_Event_User.Field.USER_ID)
                                                ).get(User.Field.LOGIN.get_key());
                                    } catch (Exception e) {
                                    }
                                return null;
                                }
                        )
                        .collect(Collectors.toList());
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
