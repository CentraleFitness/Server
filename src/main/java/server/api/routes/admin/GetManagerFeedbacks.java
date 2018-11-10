package server.api.routes.admin;

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
import protocol.admin.Protocol;

import java.util.*;

import static com.mongodb.client.model.Indexes.descending;
import static com.mongodb.client.model.Sorts.orderBy;

public class GetManagerFeedbacks {
    public GetManagerFeedbacks(Router router) {
        router.route(HttpMethod.GET, Protocol.Path.MANAGER_FEEDBACK.path).handler(routingContext -> {

            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Administrator admin;

            try {
                admin = (Administrator) Database.find_entity(Database.Collections.Administrators, Administrator.Field.EMAIL, Token.decodeToken(routingContext.request().getParam(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(admin.getField(Administrator.Field.TOKEN), routingContext.request().getParam(Protocol.Field.TOKEN.key))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);

                } else {

                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, protocol.intranet.Protocol.Status.GENERIC_OK.code);

                    @SuppressWarnings("unchecked")
                    FindIterable<Fitness_Center> findIterableCenter = (FindIterable<Fitness_Center>) Database.collections.get(Database.Collections.Fitness_Centers).find();
                    Map<String,Object> centers = new HashMap<>();
                    for (Document doc : findIterableCenter) {
                        centers.put(doc.getObjectId("_id").toString(), doc);
                    }

                    @SuppressWarnings("unchecked")
                    FindIterable<Fitness_Center_Manager> findIterableManagers = (FindIterable<Fitness_Center_Manager>) Database.collections.get(Database.Collections.Fitness_Center_Managers).find();
                    Map<String, Object> managers = new HashMap<>();
                    for (Document doc : findIterableManagers) {
                        managers.put(doc.getObjectId("_id").toString(), doc.getString("first_name") + " " + doc.getString("last_name"));
                    }

                    @SuppressWarnings("unchecked")
                    FindIterable<Feedback_State> findIterableFeedbackStates = (FindIterable<Feedback_State>) Database.collections.get(Database.Collections.Feedback_States).find();
                    Map<String, Object> feedback_states = new HashMap<>();
                    for (Document doc : findIterableFeedbackStates) {
                        feedback_states.put(doc.getObjectId("_id").toString(), doc.getString("text_fr"));
                    }

                    @SuppressWarnings("unchecked")
                    FindIterable<Feedback> findIterable = (FindIterable<Feedback>) Database.collections.get(Database.Collections.Feedbacks).find().sort(orderBy(descending(Feedback.Field.UPDATE_DATE.get_key())));
                    List<Map<String,Object>> feedbacks = new ArrayList<>();
                    HashMap<String,Object> cur;
                    for (Document doc : findIterable) {
                        cur = new HashMap<>();
                        cur.put("_id", doc.getObjectId("_id").toString());
                        cur.put("title", doc.getString("title"));
                        cur.put("description", doc.getString("description"));
                        cur.put("feedback_state_id", doc.getObjectId("feedback_state_id").toString());
                        cur.put("feedback_state", doc.getInteger("feedback_state"));
                        cur.put("fitness_manager_id", doc.getObjectId("fitness_manager_id").toString());
                        cur.put("fitness_center_id", doc.getObjectId("fitness_center_id").toString());
                        cur.put("update_date", doc.getLong("update_date"));

                        if (doc.getObjectId("fitness_manager_id") != null &&
                                managers.containsKey(doc.getObjectId("fitness_manager_id").toString())) {

                            cur.put("fitness_manager_name", managers.get(doc.getObjectId("fitness_manager_id").toString()));
                        }

                        if (doc.getObjectId("fitness_center_id") != null &&
                                centers.containsKey(doc.getObjectId("fitness_center_id").toString())) {

                            cur.put("fitness_center", centers.get(doc.getObjectId("fitness_center_id").toString()));
                        }

                        if (doc.getObjectId("feedback_state_id") != null &&
                                feedback_states.containsKey(doc.getObjectId("feedback_state_id").toString())) {

                            cur.put("feedback_state_name", feedback_states.get(doc.getObjectId("feedback_state_id").toString()));
                        }

                        feedbacks.add(cur);
                    }
                    sending.put(Protocol.Field.FEEDBACKS.key, feedbacks);
                }

            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdSerializer()).create().toJson(sending));
        });
    }
}
