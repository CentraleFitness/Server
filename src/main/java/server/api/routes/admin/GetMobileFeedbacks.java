package server.api.routes.admin;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.client.FindIterable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.admin.Protocol;

import java.util.*;

import static com.mongodb.client.model.Indexes.descending;
import static com.mongodb.client.model.Sorts.orderBy;

public class GetMobileFeedbacks {
    public GetMobileFeedbacks(Router router) {
        router.route(HttpMethod.GET, Protocol.Path.MOBILE_FEEDBACK.path).handler(routingContext -> {

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

                    LogManager.write("1");

                    @SuppressWarnings("unchecked")
                    FindIterable<Fitness_Center> findIterableCenter = (FindIterable<Fitness_Center>) Database.collections.get(Database.Collections.Fitness_Centers).find();
                    Map<String,Object> centers = new HashMap<>();
                    for (Document doc : findIterableCenter) {
                        centers.put(doc.getObjectId("_id").toString(), doc);
                    }

                    LogManager.write("2");

                    @SuppressWarnings("unchecked")
                    FindIterable<User> findIterableusers = (FindIterable<User>) Database.collections.get(Database.Collections.Users).find();
                    Map<String, Object> users = new HashMap<>();
                    for (Document doc : findIterableusers) {
                        users.put(doc.getString("email_address"), doc);
                    }

                    LogManager.write("3");

                    @SuppressWarnings("unchecked")
                    FindIterable<MobileFeedback> findIterable = (FindIterable<MobileFeedback>) Database.collections.get(Database.Collections.MobileFeedbacks).find().sort(orderBy(descending(MobileFeedback.Field.DATE.get_key())));
                    List<Map<String,Object>> feedbacks = new ArrayList<>();
                    HashMap<String,Object> cur;
                    Document user = null;
                    for (Document doc : findIterable) {
                        cur = new HashMap<>();
                        cur.put("_id", doc.getObjectId("_id").toString());
                        cur.put("email", doc.getString("email"));
                        cur.put("content", doc.getString("content"));
                        cur.put("date", doc.getString("date"));
                        cur.put("version", doc.getString("version"));
                        cur.put("__v", doc.getInteger("__v"));

                        LogManager.write("4");

                        if (doc.getString("email") != null && !doc.getString("email").equals("") &&
                                users.containsKey(doc.getString("email"))) {

                            cur.put("user", users.get(doc.getString("email")));
                        }

                        LogManager.write("5");

                        user = null;
                        if (users.containsKey(doc.getString("email"))) {
                            user = (Document) users.get(doc.getString("email"));
                        }

                        LogManager.write("6");

                        if (user != null && doc.getString("email") != null && !doc.getString("email").equals("") &&
                                centers.containsKey(user.getObjectId("fitness_center_id").toString())) {


                            cur.put("fitness_center", centers.get(user.getObjectId("fitness_center_id").toString()));
                        }

                        LogManager.write("7");

                        feedbacks.add(cur);
                    }
                    sending.put(Protocol.Field.FEEDBACKS.key, feedbacks);
                    sending.put(Protocol.Field.USERS.key, users);
                    sending.put(Protocol.Field.CENTERS.key, centers);
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
