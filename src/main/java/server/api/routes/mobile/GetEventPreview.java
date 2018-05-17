package server.api.routes.mobile;

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

import java.util.Map;

public class GetEventPreview {
    public GetEventPreview(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.GET_EVENTPREVIEW.path).handler(routingContext -> {

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
                Event event = (Event) Database.find_entity(Database.Collections.Events, User.Field.ID, eventId);
                if (user == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Unknown event id");
                    break label;
                }
                TUPLE_Event_User eventParticipation = (TUPLE_Event_User) Database.find_entity(Database.Collections.TUPLE_Event_Users, TUPLE_Event_User.Field.ID, eventId);
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.EVENTDESCRIPTION.key, event.getField(Event.Field.DESCRIPTION));
                sending.put(Protocol.Field.EVENTPICTURE.key, event.getField(Event.Field.PICTURE));
                sending.put(Protocol.Field.EVENTSTARTDATE.key, event.getField(Event.Field.START_DATE));
                sending.put(Protocol.Field.EVENTENDDATE.key, event.getField(Event.Field.END_DATE));
                sending.put(Protocol.Field.EVENTUSERREGISTERED.key, eventParticipation!=null);
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
