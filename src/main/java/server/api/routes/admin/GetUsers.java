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

public class GetUsers {
    public GetUsers(Router router) {
        router.route(HttpMethod.GET, Protocol.Path.USER.path).handler(routingContext -> {

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

                    FindIterable<Fitness_Center> findIterableCenter = (FindIterable<Fitness_Center>) Database.collections.get(Database.Collections.Fitness_Centers).find();
                    Map<String,Object> centers = new HashMap<>();
                    for (Document doc : findIterableCenter) {
                        centers.put(doc.getObjectId("_id").toString(), doc);
                    }

                    Bson posts_filter = Filters.and(
                            Filters.or(
                                Filters.eq(Post.Field.IS_CENTER.get_key(), null),
                                Filters.eq(Post.Field.IS_CENTER.get_key(), false)
                            )
                    );

                    HashMap<String,Integer> post_reports = new HashMap<>();
                    FindIterable<Post> findIterablePosts = (FindIterable<Post>) Database.collections.get(Database.Collections.Posts).find(posts_filter);
                    for (Document doc : findIterablePosts) {

                        if (!post_reports.containsKey(doc.getObjectId("posterId").toString())) {
                            post_reports.put(doc.getObjectId("posterId").toString(), 0);
                        }
                        if ((doc.getBoolean("is_center") == null || !doc.getBoolean("is_center")) &&
                                (doc.get("is_reported")) != null && ((ArrayList<ObjectId>)doc.get("is_reported")).size() > 0) {
                            post_reports.put(
                                    doc.getObjectId("posterId").toString(),
                                    post_reports.get(doc.getObjectId("posterId").toString()) + ((ArrayList<ObjectId>)doc.get("is_reported")).size()
                            );
                        }
                    }

                    @SuppressWarnings("unchecked")
                    FindIterable<User> findIterable = (FindIterable<User>) Database.collections.get(Database.Collections.Users).find();
                    List<Map<String,Object>> users = new ArrayList<>();
                    HashMap<String,Object> cur;
                    for (Document doc : findIterable) {
                        cur = new HashMap<>();
                        cur.put("_id", doc.getObjectId("_id").toString());
                        if (doc.getObjectId("fitness_center_id") != null) {
                            cur.put("fitness_center_id", doc.getObjectId("fitness_center_id").toString());
                        }
                        cur.put("login", doc.getString("login"));
                        cur.put("first_name", doc.getString("first_name"));
                        cur.put("last_name", doc.getString("last_name"));
                        cur.put("email_address", doc.getString("email_address"));
                        cur.put("picture_id", doc.getObjectId("picture_id"));

                        cur.put("reported_by_club",
                                (doc.getBoolean("reported_by_club") == null ?
                                        false : doc.getBoolean("reported_by_club")));

                        if (doc.getObjectId("fitness_center_id") != null &&
                                centers.containsKey(doc.getObjectId("fitness_center_id").toString())) {

                            cur.put("fitness_center", centers.get(doc.getObjectId("fitness_center_id").toString()));
                        }
                        cur.put("nb_report",
                                (post_reports.containsKey(doc.getObjectId("_id").toString()) ?
                                post_reports.get(doc.getObjectId("_id").toString()) : 0) +
                                        (doc.getBoolean("reported_by_club") != null &&
                                                doc.getBoolean("reported_by_club") ? 1 : 0)
                        );

                        users.add(cur);
                    }

                    class SortByNbReport implements Comparator<Map<String,Object>>
                    {
                        public int compare(Map<String,Object> a, Map<String,Object> b)
                        {
                            return (Integer) b.get("nb_report") - (Integer) a.get("nb_report");
                        }
                    }

                    users.sort(new SortByNbReport());

                    sending.put(Protocol.Field.USERS.key, users);
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
