package server.api.routes.intranet.event;

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
                } else if (!((Boolean)manager.getField(Fitness_Center_Manager.Field.IS_ACTIVE))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_ACCOUNT_INACTIVE.code);

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
                                Filters.or(
                                    Filters.eq(Event.Field.IS_DELETED.get_key(), false),
                                    Filters.eq(Event.Field.IS_DELETED.get_key(), null)
                                )
                        );
                        @SuppressWarnings("unchecked")
                        ArrayList<Document> events = (ArrayList<Document>) Database.collections.get(Database.Collections.Events).find(event_filter).into(new ArrayList<Document>());

                        if ((Database.collections.get(Database.Collections.Events).countDocuments(event_filter)) > 0) {
                            List<Bson> filters = new ArrayList<>();
                            HashMap<String, AtomicInteger> counter = new HashMap<>();
                            HashMap<String, Long> last_post = new HashMap<>();

                            for (Document cur :events) {
                                counter.put(cur.get(Event.Field.ID.get_key()).toString(), new AtomicInteger(0));
                                last_post.put(cur.get(Event.Field.ID.get_key()).toString(), 0L);
                                filters.add(Filters.eq(TUPLE_Event_User.Field.EVENT_ID.get_key(), cur.get(Event.Field.ID.get_key())));
                            }
                            DisplayConfiguration configuration = (DisplayConfiguration) Database.find_entity(Database.Collections.DisplayConfigurations, DisplayConfiguration.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));

                            @SuppressWarnings("unchecked")
                            ArrayList<ObjectId> selected_events = (ArrayList<ObjectId>)configuration.getField(DisplayConfiguration.Field.SELECTED_EVENTS);
                            @SuppressWarnings("unchecked")
                            FindIterable<Document> event_users = Database.collections.get(Database.Collections.TUPLE_Event_Users).find(Filters.or(filters));

                            Bson posts_filter = Filters.and(
                                Filters.or(filters),
                                Filters.or(
                                    Filters.eq(Post.Field.IS_DELETED.get_key(), false),
                                    Filters.eq(Post.Field.IS_DELETED.get_key(), null)
                                )
                            );

                            @SuppressWarnings("unchecked")
                            FindIterable<Document> event_posts = Database.collections.get(Database.Collections.Posts).find(posts_filter);

                            for (Document entity : event_users) {
                                counter.get(entity.get(TUPLE_Event_User.Field.EVENT_ID.get_key()).toString()).incrementAndGet();
                            }

                            for (Document entity : event_posts) {
                                if (last_post.get(entity.get(Post.Field.EVENT_ID.get_key()).toString()) < (Long)entity.get(Post.Field.DATE.get_key())) {
                                    last_post.put(entity.get(Post.Field.EVENT_ID.get_key()).toString(), (Long)entity.get(Post.Field.DATE.get_key()));
                                }
                            }

                            for (Document cur : events) {
                                cur.put(Protocol.Field.NB_SUBSCRIBERS.key, counter.get(cur.get(Event.Field.ID.get_key()).toString()).get());
                                cur.put(Protocol.Field.LAST_POST.key, last_post.get(cur.get(Event.Field.ID.get_key()).toString()));
                                if (selected_events.contains(cur.get(Event.Field.ID.get_key()))) {
                                    cur.put(Protocol.Field.SELECTED.key, true);
                                } else {
                                    cur.put(Protocol.Field.SELECTED.key, false);
                                }
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
