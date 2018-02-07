package server.api.routes.intranet;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.QueryBuilder;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
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
import protocol.ProtocolIntranet;
import protocol.intranet.ResponseObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GetEvents {
    public GetEvents(Router router) {
        router.route(HttpMethod.POST, ProtocolIntranet.Path.GET_EVENTS.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Fitness_Center center;
            Database database = Database.getInstance();

            try {
                manager = (Fitness_Center_Manager) database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(ProtocolIntranet.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(ProtocolIntranet.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.AUTH_ERROR_TOKEN.code);
                } else {

                    center = (Fitness_Center) database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.MGR_ERROR_NO_CENTER.code);
                    } else {
                        sending = new ResponseObject(false);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_OK.code);

                        LinkedList<Database.Entity> events = database.find_entities(Database.Collections.Events, Event.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));

                        if ((events.size()) > 0) {
                            List<Bson> filters = new ArrayList<>();
                            HashMap<String, AtomicInteger> counter = new HashMap<>();

                            for (Database.Entity cur :events) {
                                counter.put(cur.getField(Event.Field.ID).toString(), new AtomicInteger(0));
                                filters.add(Filters.eq(TUPLE_Event_User.Field.EVENT_ID.get_key(), cur.getField(Event.Field.ID)));
                            }
                            @SuppressWarnings("unchecked")
                            FindIterable<Document> event_users = Database.getInstance().collections.get(Database.Collections.TUPLE_Event_Users).find(Filters.or(filters));

                            for (Document entity : event_users) {
                                counter.get(entity.get(TUPLE_Event_User.Field.EVENT_ID.get_key()).toString()).incrementAndGet();
                            }

                            for (Database.Entity cur :events) {
                                cur.put(ProtocolIntranet.Field.NB_SUBSCRIBERS.key, counter.get(cur.getField(Event.Field.ID).toString()).get());
                            }
                        }

                        sending.put(ProtocolIntranet.Field.EVENTS.key, events);
                    }
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdSerializer()).create().toJson(sending));
        });
    }
}
