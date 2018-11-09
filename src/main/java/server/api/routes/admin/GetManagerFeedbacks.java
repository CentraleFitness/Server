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
import model.entities.Administrator;
import model.entities.Feedback;
import model.entities.Fitness_Center_Manager;
import model.entities.Module;
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

                    Bson filter = Filters.and();

                    if (routingContext.request().getParam(Protocol.Field.FITNESS_CENTER_ID.key) != null) {

                        Fitness_Center_Manager manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.FITNESS_CENTER_ID, routingContext.request().getParam(Protocol.Field.FITNESS_CENTER_ID.key));

                        filter = Filters.and(
                                Filters.eq(Feedback.Field.FITNESS_MANAGER_ID.get_key(), manager.getField(Fitness_Center_Manager.Field.ID))
                        );

                    }

                    @SuppressWarnings("unchecked")
                    FindIterable<Feedback> findIterable = (FindIterable<Feedback>) Database.collections.get(Database.Collections.Feedbacks).find(filter).sort(orderBy(descending(Feedback.Field.UPDATE_DATE.get_key())));
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
                        cur.put("update_date", doc.getLong("update_date"));

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
