package server.api.routes.mobile.event;

import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;

import com.auth0.jwt.JWT;
import com.google.gson.GsonBuilder;

import Tools.LogManager;
import Tools.Token;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.Database.Collections;
import model.entities.Event;
import model.entities.Fitness_Center;
import model.entities.TUPLE_Event_User;
import model.entities.User;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

public class EventRegistration {
    public EventRegistration(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.EVENT_REGISTRATION.path).handler(routingContext -> {

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
                Event event = (Event) Database.find_entity(Collections.Events, Fitness_Center.Field.ID, eventId);
                if (event == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.EVENT_NOT_FOUND.code);
                    LogManager.write(Protocol.Status.EVENT_NOT_FOUND.message);
                    break label;
                }
                TUPLE_Event_User tuple_Event_User = (TUPLE_Event_User) Database.new_entity(Collections.TUPLE_Event_Users);
                tuple_Event_User.setField(TUPLE_Event_User.Field.EVENT_ID, eventId);
                tuple_Event_User.setField(TUPLE_Event_User.Field.USER_ID, user.getField(User.Field.ID));
                Database.update_entity(Collections.TUPLE_Event_Users, tuple_Event_User);
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
