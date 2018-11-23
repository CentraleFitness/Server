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

                    LogManager.write("GETUSERS 1");

                    FindIterable<Fitness_Center> findIterableCenter = (FindIterable<Fitness_Center>) Database.collections.get(Database.Collections.Fitness_Centers).find();
                    Map<String,Object> centers = new HashMap<>();
                    for (Document doc : findIterableCenter) {
                        centers.put(doc.getObjectId("_id").toString(), doc);
                    }

                    LogManager.write("GETUSERS 2");

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
                        LogManager.write("GETUSERS 3");
                        if ((doc.getBoolean("is_center") == null || !doc.getBoolean("is_center")) &&
                                (doc.get("is_reported")) != null && ((ArrayList<ObjectId>)doc.get("is_reported")).size() > 0) {
                            post_reports.put(
                                    doc.getObjectId("posterId").toString(),
                                    post_reports.get(doc.getObjectId("posterId").toString()) + ((ArrayList<ObjectId>)doc.get("is_reported")).size()
                            );
                        }
                        LogManager.write("GETUSERS 4");
                    }
                    LogManager.write("GETUSERS 5");

                    @SuppressWarnings("unchecked")
                    FindIterable<User> findIterable = (FindIterable<User>) Database.collections.get(Database.Collections.Users).find();
                    List<Map<String,Object>> users = new ArrayList<>();
                    HashMap<String,Object> cur;
                    for (Document doc : findIterable) {
                        cur = new HashMap<>();
                        cur.put("_id", doc.getObjectId("_id").toString());
                        LogManager.write("GETUSERS 6");
                        cur.put("fitness_center_id", doc.getObjectId("fitness_center_id").toString());
                        LogManager.write("GETUSERS 7");
                        cur.put("login", doc.getString("login"));
                        LogManager.write("GETUSERS 8");
                        cur.put("first_name", doc.getString("first_name"));
                        LogManager.write("GETUSERS 9");
                        cur.put("last_name", doc.getString("last_name"));
                        LogManager.write("GETUSERS 10");
                        cur.put("email_address", doc.getString("email_address"));
                        LogManager.write("GETUSERS 11");
                        cur.put("picture_id", doc.getObjectId("picture_id"));
                        LogManager.write("GETUSERS 12");

                        cur.put("reported_by_club",
                                (doc.getBoolean("reported_by_club") == null ?
                                        false : doc.getBoolean("reported_by_club")));

                        LogManager.write("GETUSERS 13");
                        if (centers.containsKey(doc.getObjectId("fitness_center_id").toString())) {
                            cur.put("fitness_center", centers.get(doc.getObjectId("fitness_center_id").toString()));
                        }
                        LogManager.write("GETUSERS 14");
                        cur.put("nb_report",
                                (post_reports.containsKey(doc.getObjectId("_id").toString()) ?
                                post_reports.get(doc.getObjectId("_id").toString()) : 0) +
                                        (doc.getBoolean("reported_by_club") != null &&
                                                doc.getBoolean("reported_by_club") ? 1 : 0)
                        );
                        LogManager.write("GETUSERS 15");

                        users.add(cur);
                    }

                    class SortByNbReport implements Comparator<Map<String,Object>>
                    {
                        public int compare(Map<String,Object> a, Map<String,Object> b)
                        {
                            return (Integer) b.get("nb_report") - (Integer) a.get("nb_report");
                        }
                    }

                    LogManager.write("GETUSERS 16");
                    users.sort(new SortByNbReport());
                    LogManager.write("GETUSERS 17");

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
