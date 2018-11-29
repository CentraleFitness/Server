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

                    FindIterable<Administrator> findIterableAdmin = (FindIterable<Administrator>) Database.collections.get(Database.Collections.Administrators).find();
                    Map<String,Object> admins = new HashMap<>();
                    for (Document doc : findIterableAdmin) {
                        admins.put(doc.getObjectId("_id").toString(),
                                doc.getString("first_name") + " " + doc.getString("last_name")
                        );
                    }

                    FindIterable<Fitness_Center> findIterableCenter = (FindIterable<Fitness_Center>) Database.collections.get(Database.Collections.Fitness_Centers).find();
                    Map<String,Object> centers = new HashMap<>();
                    for (Document doc : findIterableCenter) {
                        centers.put(doc.getObjectId("_id").toString(), doc);
                    }

                    FindIterable<Picture> findIterablePicture = (FindIterable<Picture>) Database.collections.get(Database.Collections.Pictures).find();
                    Map<String,Object> pictures = new HashMap<>();
                    for (Document doc : findIterablePicture) {
                        pictures.put(doc.getObjectId("_id").toString(), doc.getString("picture"));
                    }

                    Bson posts_filter = Filters.and(
                            Filters.or(
                                Filters.eq(Post.Field.IS_CENTER.get_key(), null),
                                Filters.eq(Post.Field.IS_CENTER.get_key(), false)
                            )
                    );

                    LogManager.write("E1");

                    HashMap<String,ArrayList<Object>> post_reported = new HashMap<>();
                    HashMap<String,Integer> post_reports = new HashMap<>();
                    HashMap<String,Object> cur_posts;
                    FindIterable<Post> findIterablePosts = (FindIterable<Post>) Database.collections.get(Database.Collections.Posts).find(posts_filter);
                    for (Document doc : findIterablePosts) {

                        LogManager.write("E2");

                        if (!post_reports.containsKey(doc.getObjectId("posterId").toString())) {
                            post_reports.put(doc.getObjectId("posterId").toString(), 0);
                        }

                        LogManager.write("E3");

                        if (!post_reported.containsKey(doc.getObjectId("posterId").toString())) {
                            post_reported.put(doc.getObjectId("posterId").toString(), new ArrayList<>());
                        }

                        LogManager.write("E4");

                        if ((doc.get("is_reported") != null && ((ArrayList<ObjectId>)doc.get("is_reported")).size() > 0) ||
                                (doc.getBoolean("reported_by_club") != null && doc.getBoolean("reported_by_club"))) {

                            LogManager.write("E5");

                            cur_posts = new HashMap<>();
                            cur_posts.put("_id", doc.getObjectId("_id").toString());
                            LogManager.write("E5.1");
                            cur_posts.put("posterName", doc.getString("posterName"));
                            LogManager.write("E5.2");
                            cur_posts.put("is_comment", (doc.getBoolean("is_comment") != null && doc.getBoolean("is_comment")));
                            LogManager.write("E5.3");
                            cur_posts.put("type", doc.getString("type"));
                            LogManager.write("E5.4");
                            cur_posts.put("date", doc.getLong("date"));
                            LogManager.write("E5.5");
                            cur_posts.put("content", doc.getString("content"));
                            LogManager.write("E5.6");
                            cur_posts.put("title", doc.getString("title"));
                            LogManager.write("E5.7");
                            cur_posts.put("picture", doc.getString("picture"));
                            LogManager.write("E5.8");
                            if (doc.getObjectId("event_id") != null) {
                                cur_posts.put("event_id", doc.getObjectId("event_id").toString());
                                LogManager.write("E5.9");
                                cur_posts.put("start_date", doc.getLong("start_date"));
                                LogManager.write("E5.10");
                                cur_posts.put("end_date", doc.getLong("end_date"));
                            }

                            LogManager.write("E6");

                            cur_posts.put("nb_likes",
                                    (doc.get("likes") != null ? ((ArrayList<ObjectId>)doc.get("likes")).size() : 0) +
                                    (doc.getBoolean("likedByClub") != null && doc.getBoolean("likedByClub") ? 1 : 0)
                            );

                            LogManager.write("E7");

                            cur_posts.put("nb_comments",
                                    (doc.get("comments") != null ? ((ArrayList<ObjectId>)doc.get("comments")).size() : 0));

                            LogManager.write("E8");

                            cur_posts.put("nb_reports",
                                    (doc.get("is_reported") != null ? ((ArrayList<ObjectId>)doc.get("is_reported")).size() : 0) +
                                    (doc.getBoolean("reported_by_club") != null && doc.getBoolean("reported_by_club") ? 1 : 0)
                            );

                            LogManager.write("E9");

                            post_reported.get(doc.getObjectId("posterId").toString()).add(cur_posts);

                            LogManager.write("E10");

                            post_reports.put(
                                    doc.getObjectId("posterId").toString(),
                                    post_reports.get(doc.getObjectId("posterId").toString()) +
                                            (doc.get("is_reported") != null ? ((ArrayList<ObjectId>)doc.get("is_reported")).size() : 0) +
                                            (doc.getBoolean("reported_by_club") != null && doc.getBoolean("reported_by_club") ? 1 : 0)
                            );

                            LogManager.write("E11");
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
                        if (doc.getLong("creation_date") != null) {
                            cur.put("creation_date", doc.getLong("creation_date"));
                        }
                        cur.put("is_active", doc.getBoolean("is_active") != null && doc.getBoolean("is_active"));

                        if (doc.getLong("last_update_activity") != null) {
                            cur.put("last_update_activity", doc.getLong("last_update_activity"));
                        }
                        if (doc.getObjectId("last_update_admin_id") != null) {
                            cur.put("last_update_admin_id", doc.getObjectId("last_update_admin_id").toString());

                            if (admins.containsKey(doc.getObjectId("last_update_admin_id").toString())) {
                                cur.put("last_update_admin_name",
                                        admins.get(doc.getObjectId("last_update_admin_id").toString()));
                            }
                        }

                        cur.put("login", doc.getString("login"));
                        cur.put("first_name", doc.getString("first_name"));
                        cur.put("last_name", doc.getString("last_name"));
                        cur.put("email_address", doc.getString("email_address"));
                        cur.put("picture_id", doc.getObjectId("picture_id"));

                        if (doc.getObjectId("picture_id") != null &&
                                pictures.containsKey(doc.getObjectId("picture_id").toString())) {

                            cur.put("picture", pictures.get(doc.getObjectId("picture_id").toString()));
                        } else {
                            cur.put("picture", "");
                        }

                        if (doc.getObjectId("fitness_center_id") != null &&
                                centers.containsKey(doc.getObjectId("fitness_center_id").toString())) {

                            cur.put("fitness_center", centers.get(doc.getObjectId("fitness_center_id").toString()));
                        }
                        cur.put("nb_report",
                                (post_reports.containsKey(doc.getObjectId("_id").toString()) ?
                                post_reports.get(doc.getObjectId("_id").toString()) : 0)
                        );
                        cur.put("reported_posts",
                                (post_reported.containsKey(doc.getObjectId("_id").toString()) ?
                                        post_reported.get(doc.getObjectId("_id").toString()) :
                                        new ArrayList<>())
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
