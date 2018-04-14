package server.api.routes.unfinished.mobile;

import Tools.LogManager;
import Tools.Token;
import com.auth0.jwt.JWT;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Event;
import model.entities.Fitness_Center;
import model.entities.TUPLE_Event_User;
import model.entities.User;
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.mobile.Protocol;
import static com.mongodb.client.model.Filters.eq;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mongodb.client.model.Filters.eq;

public class GetEvents {
    public GetEvents(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.GET_EVENTS.path).handler(routingContext -> {

            ResponseObject sending = null;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            label:try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rToken = (String) received.get(Protocol.Field.TOKEN.key);
                Integer rStart = (Integer) received.get(Protocol.Field.START.key);
                Integer rEnd = (Integer) received.get(Protocol.Field.END.key);

                if (rToken == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.TOKEN.key);
                    break label;
                }
                if (rStart == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.START.key);
                    break label;
                }
                if (rEnd == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.END.key);
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
                ObjectId fitnessCenterId = (ObjectId) user.getField(User.Field.FITNESS_CENTER_ID);
                if (fitnessCenterId == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.NO_AFFILIATION.code);
                    LogManager.write(Protocol.Status.NO_AFFILIATION.message);
                    break label;
                }
                List<List> eventList =
                        (List<List>) Database
                                .collections
                                .get(Database.Collections.Events)
                                .find(eq(Event.Field.FITNESS_CENTER_ID.get_key(), fitnessCenterId))
                                .sort(new BasicDBObject(Event.Field.FITNESS_CENTER_ID.get_key(), 1))
                                .skip(rStart)
                                .limit(rEnd)
                                .into(new ArrayList())
                                .stream()
                                .map(doc -> {
                                    Event event = new Event((Document) doc);
                                    return Stream.of(event.getField(Event.Field.TITLE), event.getField(Event.Field.ID)).collect(Collectors.toList());
                                })
                        .collect(Collectors.toList());
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                sending.put(Protocol.Field.EVENTS.key, eventList);
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
                LogManager.write(e);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
