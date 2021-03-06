package server.api.routes.intranet.publication;

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

import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

public class CenterGetPublications {
    public CenterGetPublications(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.CENTER_GET_PUBLICATIONS.path).handler(routingContext -> {
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

                        Bson posts_filter = Filters.and(
                            Filters.eq(Post.Field.FITNESS_CENTERT_ID.get_key(), center.getField(Fitness_Center.Field.ID)),

                            Filters.or(
                                Filters.eq(Post.Field.IS_COMMENT.get_key(), false),
                                Filters.eq(Post.Field.IS_COMMENT.get_key(), null)
                            ),

                            Filters.or(
                                Filters.eq(Post.Field.IS_DELETED.get_key(), false),
                                Filters.eq(Post.Field.IS_DELETED.get_key(), null)
                            )
                        );

                        List<ObjectId> comments_list = new ArrayList<>();
                        List<ObjectId> users_list = new ArrayList<>();
                        List<ObjectId> centers_list = new ArrayList<>();
                        List<ObjectId> event_list = new ArrayList<>();
                        List<ObjectId> cur_com;

                        FindIterable<Post> findIterable = (FindIterable<Post>) Database.collections.get(Database.Collections.Posts).find(posts_filter).sort(orderBy(ascending(Post.Field.DATE.get_key())));
                        for (Document doc : findIterable) {

                            if (doc.getBoolean("is_center") != null && doc.getBoolean("is_center")) {
                                centers_list.add(doc.getObjectId("fitness_center_id"));
                            } else {
                                users_list.add(doc.getObjectId("posterId"));
                            }

                            if ((cur_com = (List<ObjectId>)doc.get("comments")) != null &&
                                    cur_com.size() > 0) {

                                comments_list.addAll(cur_com);
                            }

                            if (doc.getObjectId("event_id") != null) {
                                event_list.add(doc.getObjectId("event_id"));
                            }
                        }

                        Bson users_filter = Filters.and(
                            Filters.in(User.Field.ID.get_key(), users_list)
                        );

                        List<ObjectId> pictures_list = new ArrayList<>();
                        HashMap<String,Object> users = new HashMap<>();
                        FindIterable<User> findIterableUsers = (FindIterable<User>) Database.collections.get(Database.Collections.Users).find(users_filter);
                        for (Document doc : findIterableUsers) {

                            users.put(doc.getObjectId("_id").toString(), doc);
                            pictures_list.add(doc.getObjectId("picture_id"));
                        }

                        Bson events_filter = Filters.and(
                                Filters.in(Event.Field.ID.get_key(), event_list)
                        );

                        HashMap<String,Object> events = new HashMap<>();
                        FindIterable<Event> findIterableEvents = (FindIterable<Event>) Database.collections.get(Database.Collections.Events).find(events_filter);
                        for (Document doc : findIterableEvents) {

                            events.put(doc.getObjectId("_id").toString(), doc);
                        }

                        Bson centers_filter = Filters.and(
                            Filters.in(Fitness_Center.Field.ID.get_key(), centers_list)
                        );

                        HashMap<String,Object> centers = new HashMap<>();
                        FindIterable<Fitness_Center> findIterableCenters = (FindIterable<Fitness_Center>) Database.collections.get(Database.Collections.Fitness_Centers).find(centers_filter);
                        for (Document doc : findIterableCenters) {

                            centers.put(doc.getObjectId("_id").toString(), doc);
                            pictures_list.add(doc.getObjectId("picture_id"));
                        }

                        Bson pictures_filter = Filters.and(
                            Filters.in(Picture.Field.ID.get_key(), pictures_list)
                        );

                        HashMap<String,Object> pictures = new HashMap<>();
                        FindIterable<Picture> findIterablePictures = (FindIterable<Picture>) Database.collections.get(Database.Collections.Pictures).find(pictures_filter);
                        for (Document doc : findIterablePictures) {
                            pictures.put(doc.getObjectId("_id").toString(), doc.getString("picture"));
                        }

                        Bson comments_filter = Filters.and(
                            Filters.in(Post.Field.ID.get_key(), comments_list),

                            Filters.or(
                                    Filters.eq(Post.Field.IS_DELETED.get_key(), false),
                                    Filters.eq(Post.Field.IS_DELETED.get_key(), null)
                            )
                        );

                        Document tmpUser;

                        HashMap<String,Object> comments = new HashMap<>();
                        FindIterable<Post> findIterableComments = (FindIterable<Post>) Database.collections.get(Database.Collections.Posts).find(comments_filter);
                        for (Document doc : findIterableComments) {

                            //doc.put("isMine", doc.getObjectId("posterId").toString().equals(manager.getField(Fitness_Center_Manager.Field.ID).toString()));
                            doc.put("isMine", true);

                            if (doc.getBoolean("is_center") != null && doc.getBoolean("is_center")) {
                                tmpUser = (Document)centers.get(doc.getObjectId("fitness_center_id").toString());
                            } else {
                                tmpUser = (Document)users.get(doc.getObjectId("posterId").toString());
                            }

                            if (tmpUser == null || tmpUser.getObjectId("picture_id") == null ||
                                    tmpUser.getObjectId("picture_id").toString().equals("")) {

                                doc.put("posterPicture", "");
                            } else {
                                doc.put("posterPicture", pictures.get(tmpUser.getObjectId("picture_id").toString()));
                            }

                            if (doc.getBoolean("reported_by_club") == null) {
                                doc.put("reported_by_club", false);
                            }

                            comments.put(doc.getObjectId("_id").toString(), doc);
                        }

                        List<Object> cur_comments;
                        List<ObjectId> li;
                        Integer li_size;

                        HashMap<String, Object> cur;
                        List<Map<String,Object>> posts = new ArrayList<>();
                        for (Document doc : findIterable) {
                            cur = new HashMap<>();

                            cur.put("_id", doc.getObjectId("_id").toString());
                            cur.put("fitness_center_id", doc.getObjectId("fitness_center_id").toString());
                            cur.put("is_center", doc.getBoolean("is_center"));
                            cur.put("posterId", doc.getObjectId("posterId").toString());
                            cur.put("isMine", true);
                            //cur.put("isMine", doc.getObjectId("posterId").toString().equals(manager.getField(Fitness_Center_Manager.Field.ID).toString()));
                            cur.put("posterName", doc.getString("posterName"));

                            cur.put("likedByMe", (doc.getBoolean("likedByClub") != null && doc.getBoolean("likedByClub")));

                            if (doc.getBoolean("is_center") != null && doc.getBoolean("is_center")) {
                                tmpUser = (Document)centers.get(doc.getObjectId("fitness_center_id").toString());
                            } else {
                                tmpUser = (Document)users.get(doc.getObjectId("posterId").toString());
                            }

                            if (tmpUser == null || tmpUser.getObjectId("picture_id") == null ||
                                    tmpUser.getObjectId("picture_id").toString().equals("")) {

                                cur.put("posterPicture", "");
                            } else {
                                cur.put("posterPicture", pictures.get(tmpUser.getObjectId("picture_id").toString()));
                            }

                            cur.put("date", doc.getLong("date"));
                            cur.put("content", doc.getString("content"));
                            cur.put("title", doc.getString("title"));

                            cur.put("reported_by_club",
                                    (doc.getBoolean("reported_by_club") != null && doc.getBoolean("reported_by_club")));

                            cur.put("type", doc.getString("type"));

                            if (doc.getString("picture") != null && doc.getObjectId("picture_id") != null) {
                                cur.put("picture", doc.getString("picture"));
                                cur.put("picture_id", doc.getObjectId("picture_id").toString());
                            }

                            if (doc.getObjectId("event_id") != null) {
                                cur.put("event_is_deleted",
                                        events.containsKey(doc.getObjectId("event_id").toString()) ?
                                        ((Document)events.get(doc.getObjectId("event_id").toString())).getBoolean("is_deleted") :
                                        true
                                );
                                cur.put("event_id", doc.getObjectId("event_id").toString());
                                cur.put("start_date", doc.getLong("start_date"));
                                cur.put("end_date", doc.getLong("end_date"));
                            }

                            li = (List<ObjectId>)doc.get("likes");
                            li_size = (li == null ? 0 : li.size());

                            cur.put("nb_likes", li_size + (
                                    doc.getBoolean("likedByClub") != null &&
                                            doc.getBoolean("likedByClub") ? 1 : 0)
                            );

                            li = (List<ObjectId>)doc.get("comments");
                            li_size = (li == null ? 0 : li.size());

                            cur.put("nb_comments", li_size);

                            cur_comments = new ArrayList<>();
                            if (li != null) {
                                for (ObjectId cur_id : li) {
                                    cur_comments.add(comments.get(cur_id.toString()));
                                }
                            }
                            cur.put("comments", cur_comments);

                            posts.add(cur);
                        }

                        sending.put(Protocol.Field.PUBLICATIONS.key, posts);
                    }
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
                System.out.println("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdSerializer()).create().toJson(sending));
        });
    }
}
