package server.api.routes.intranet;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Event;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import model.entities.TUPLE_Event_User;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GetEvents {
    public GetEvents(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.GET_EVENTS.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Fitness_Center center;

            try {
                manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else {

                    center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_NO_CENTER.code);
                    } else {
                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                        Bson event_filter = Filters.and(
                                Filters.eq(Event.Field.FITNESS_CENTER_ID.get_key(), center.getField(Fitness_Center.Field.ID)),
                                Filters.eq(Event.Field.IS_DELETED.get_key(), false)
                        );
                        @SuppressWarnings("unchecked")
                        ArrayList<Document> events = (ArrayList<Document>) Database.collections.get(Database.Collections.Events).find(event_filter).into(new ArrayList<Document>());

                        if ((Database.collections.get(Database.Collections.Events).count(event_filter)) > 0) {
                            List<Bson> filters = new ArrayList<>();
                            HashMap<String, AtomicInteger> counter = new HashMap<>();

                            for (Document cur :events) {
                                counter.put(cur.get(Event.Field.ID.get_key()).toString(), new AtomicInteger(0));
                                filters.add(Filters.eq(TUPLE_Event_User.Field.EVENT_ID.get_key(), cur.get(Event.Field.ID.get_key())));
                            }
                            @SuppressWarnings("unchecked")
                            FindIterable<Document> event_users = Database.collections.get(Database.Collections.TUPLE_Event_Users).find(Filters.or(filters));

                            for (Document entity : event_users) {
                                counter.get(entity.get(TUPLE_Event_User.Field.EVENT_ID.get_key()).toString()).incrementAndGet();
                            }

                            for (Document cur :events) {
                                cur.put(Protocol.Field.NB_SUBSCRIBERS.key, counter.get(cur.get(Event.Field.ID.get_key()).toString()).get());
                            }
                        }
                        sending.put(Protocol.Field.EVENTS.key, events);
                    }
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdSerializer()).create().toJson(sending));
        });
    }
}
