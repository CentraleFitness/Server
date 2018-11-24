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
import model.entities.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GetHomeSummary {
    public GetHomeSummary(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.GET_HOME_SUMMARY.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;

            try {
                manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else if (!((Boolean)manager.getField(Fitness_Center_Manager.Field.IS_ACTIVE))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_ACCOUNT_INACTIVE.code);

                } else {
                    Fitness_Center center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_NO_CENTER.code);
                    } else {
                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                        LinkedList<Database.Entity> users = Database.find_entities(Database.Collections.Users, User.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));
                        Long time = System.currentTimeMillis();

                        sending.put(Protocol.Field.NB_SUBSCRIBERS.key, users.size());
                        sending.put(Protocol.Field.FITNESS_CENTER_ID.key, center.getField(Fitness_Center.Field.ID).toString());

                        Bson event_filter = Filters.and(
                                Filters.eq(Event.Field.FITNESS_CENTER_ID.get_key(), center.getField(Fitness_Center.Field.ID)),
                                Filters.eq(Event.Field.IS_DELETED.get_key(), false),
                                Filters.or(
                                    Filters.gte(Event.Field.START_DATE.get_key(), time),
                                    Filters.gte(Event.Field.END_DATE.get_key(), time)
                                )
                        );
                        @SuppressWarnings("unchecked")
                        ArrayList<Document> events = (ArrayList<Document>) Database.collections.get(Database.Collections.Events).find(event_filter).limit(3).into(new ArrayList<Document>());

                        if ((Database.collections.get(Database.Collections.Events).count(event_filter)) > 0) {
                            List<Bson> filters = new ArrayList<>();
                            HashMap<String, AtomicInteger> counter = new HashMap<>();
                            HashMap<String, Long> last_post = new HashMap<>();

                            for (Document cur :events) {
                                counter.put(cur.get(Event.Field.ID.get_key()).toString(), new AtomicInteger(0));
                                last_post.put(cur.get(Event.Field.ID.get_key()).toString(), 0L);
                                filters.add(Filters.eq(TUPLE_Event_User.Field.EVENT_ID.get_key(), cur.get(Event.Field.ID.get_key())));
                            }
                            @SuppressWarnings("unchecked")
                            FindIterable<Document> event_users = Database.collections.get(Database.Collections.TUPLE_Event_Users).find(Filters.or(filters));
                            @SuppressWarnings("unchecked")
                            FindIterable<Document> event_posts = Database.collections.get(Database.Collections.Posts).find(Filters.or(filters));

                            for (Document entity : event_users) {
                                counter.get(entity.get(TUPLE_Event_User.Field.EVENT_ID.get_key()).toString()).incrementAndGet();
                            }

                            for (Document entity : event_posts) {
                                last_post.put(entity.get(Post.Field.EVENT_ID.get_key()).toString(), (Long)entity.get(Post.Field.DATE.get_key()));
                            }

                            for (Document cur :events) {
                                cur.put(Protocol.Field.NB_SUBSCRIBERS.key, counter.get(cur.get(Event.Field.ID.get_key()).toString()).get());
                                cur.put(Protocol.Field.LAST_POST.key, last_post.get(cur.get(Event.Field.ID.get_key()).toString()));
                            }
                        }
                        sending.put(Protocol.Field.EVENTS.key, events);
                        sending.put(Protocol.Field.CENTER_NAME.key, center.getField(Fitness_Center.Field.NAME));
                        sending.put(Protocol.Field.MANAGER_FIRST_NAME.key, manager.getField(Fitness_Center_Manager.Field.FIRSTNAME));
                        sending.put(Protocol.Field.MANAGER_LAST_NAME.key, manager.getField(Fitness_Center_Manager.Field.LASTNAME));
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
