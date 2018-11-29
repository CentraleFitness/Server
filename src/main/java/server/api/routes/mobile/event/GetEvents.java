package server.api.routes.mobile.event;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.auth0.jwt.JWT;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;

import Tools.LogManager;
import Tools.Token;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Event;
import model.entities.TUPLE_Event_User;
import model.entities.User;
import protocol.ResponseObject;
import protocol.mobile.Protocol;

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
                List<Event> eventList =
                        (List<Event>) Database
                                .collections
                                .get(Database.Collections.Events)
                                .find(and(
                                    eq(Event.Field.FITNESS_CENTER_ID.get_key(), fitnessCenterId),

                                    or(
                                        eq(Event.Field.IS_DELETED.get_key(), null),
                                        eq(Event.Field.IS_DELETED.get_key(), false)
                                    )
                                ))
                                .sort(new BasicDBObject(Event.Field.CREATION_DATE.get_key(), 1))
                                //.filter(eq(Event.Field.IS_DELETED.get_key(), false))
                                .skip(rStart)
                                .limit(rEnd)
                                .into(new ArrayList())
                                .stream()
                                .map(doc -> {
                                    Event event = new Event((Document) doc);
                                    boolean isreg = Optional.ofNullable(received.get(Protocol.Field.ISREG.key)).map(tt -> (boolean) tt).orElse(false);
                                    Document eventParticipation = null;
                                    if (isreg)
                                        try {
                                            eventParticipation = Database.find_entity(Database.Collections.TUPLE_Event_Users, new BasicDBObject("$and", Arrays.asList(new BasicDBObject(TUPLE_Event_User.Field.EVENT_ID.get_key(), event.getField(Event.Field.ID)), new BasicDBObject(TUPLE_Event_User.Field.USER_ID.get_key(), user.getField(User.Field.ID)))));
                                        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                                        }
                                    return Arrays.asList(event.getField(Event.Field.TITLE), event.getField(Event.Field.ID).toString(), eventParticipation!=null);
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
