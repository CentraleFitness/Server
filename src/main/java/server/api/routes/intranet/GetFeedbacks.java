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
import model.entities.Feedback;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.*;

import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.orderBy;

public class GetFeedbacks {
    public GetFeedbacks(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.GET_FEEDBACKS.path).handler(routingContext -> {
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

                        Bson managers_filter = Filters.and(
                                Filters.eq(
                                    Fitness_Center_Manager.Field.FITNESS_CENTER_ID.get_key(),
                                    center.getField(Fitness_Center.Field.ID)
                                )
                        );

                        HashMap<String,Object> managers = new HashMap<>();
                        FindIterable<Fitness_Center_Manager> findIterableManager = (FindIterable<Fitness_Center_Manager>) Database.collections.get(Database.Collections.Fitness_Center_Managers).find(managers_filter);
                        for (Document doc : findIterableManager) {
                            managers.put(doc.getObjectId("_id").toString(), doc.getString("first_name") + " " + doc.getString("last_name"));
                        }

                        Bson feedbacks_filter = Filters.and(
                                Filters.eq(
                                    Feedback.Field.FITNESS_CENTER_ID.get_key(),
                                    center.getField(Fitness_Center.Field.ID)
                                )
                        );

                        List<Document> feedbacks = new ArrayList<>();
                        FindIterable<Feedback> findIterableFeedbacks = (FindIterable<Feedback>) Database.collections.get(Database.Collections.Feedbacks).find(feedbacks_filter).sort(orderBy(descending(Feedback.Field.UPDATE_DATE.get_key())));
                        for (Document doc : findIterableFeedbacks) {

                            if (doc.getObjectId("fitness_manager_id") != null &&
                                    managers.containsKey(doc.getObjectId("fitness_manager_id").toString())) {

                                doc.put("fitness_manager_name", managers.get(doc.getObjectId("fitness_manager_id").toString()));
                            }

                            feedbacks.add(doc);
                        }

                        sending.put(Protocol.Field.FEEDBACKS.key, feedbacks);
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
