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

            LogManager.write("BEGIN");

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
                                Filters.eq(Post.Field.FITNESS_CENTERT_ID.get_key(), center.getField(Fitness_Center.Field.ID))
                        );

                        List<ObjectId> comments_list = new ArrayList<>();
                        List<ObjectId> users_list = new ArrayList<>();
                        List<ObjectId> centers_list = new ArrayList<>();
                        List<ObjectId> cur_com;

                        LogManager.write("1");

                        FindIterable<Post> findIterable = (FindIterable<Post>) Database.collections.get(Database.Collections.Posts).find(posts_filter).sort(orderBy(ascending(Post.Field.DATE.get_key())));
                        for (Document doc : findIterable) {

                            if (doc.getBoolean("is_center")) {
                                centers_list.add(doc.getObjectId("fitness_center_id"));
                            } else {
                                users_list.add(doc.getObjectId("posterId"));
                            }

                            if ((cur_com = (List<ObjectId>)doc.get("comments")) != null &&
                                    cur_com.size() > 0) {

                                comments_list.addAll(cur_com);
                            }
                        }

                        LogManager.write("2");

                        Bson users_filter = Filters.and(
                                Filters.in(User.Field.ID.get_key(), users_list)
                        );

                        List<ObjectId> pictures_list = new ArrayList<>();
                        HashMap<String,Object> users = new HashMap<>();
                        FindIterable<User> findIterableUsers = (FindIterable<User>) Database.collections.get(Database.Collections.Users).find(users_filter);
                        for (Document doc : findIterableUsers) {

                            users.put(doc.getObjectId("_id").toString(), doc);
                            users_list.add(doc.getObjectId("picture_id"));
                        }

                        LogManager.write("2");

                        Bson centers_filter = Filters.and(
                                Filters.in(Fitness_Center.Field.ID.get_key(), centers_list)
                        );

                        HashMap<String,Object> centers = new HashMap<>();
                        FindIterable<Fitness_Center> findIterableCenters = (FindIterable<Fitness_Center>) Database.collections.get(Database.Collections.Fitness_Centers).find(centers_filter);
                        for (Document doc : findIterableCenters) {

                            centers.put(doc.getObjectId("_id").toString(), doc);
                            centers_list.add(doc.getObjectId("picture_id"));
                        }

                        LogManager.write("3");

                        Bson pictures_filter = Filters.and(
                                Filters.in(Picture.Field.ID.get_key(), pictures_list)
                        );

                        HashMap<String,Object> pictures = new HashMap<>();
                        FindIterable<Picture> findIterablePictures = (FindIterable<Picture>) Database.collections.get(Database.Collections.Pictures).find(pictures_filter);
                        for (Document doc : findIterablePictures) {

                            pictures.put(doc.getObjectId("_id").toString(), doc.getString("picture"));
                        }

                        LogManager.write("4");

                        Bson comments_filter = Filters.and(
                                Filters.in(Post.Field.ID.get_key(), comments_list)
                        );

                        HashMap<String,Object> comments = new HashMap<>();
                        FindIterable<Post> findIterableComments = (FindIterable<Post>) Database.collections.get(Database.Collections.Posts).find(comments_filter);
                        for (Document doc : findIterableComments) {

                            comments.put(doc.getObjectId("_id").toString(), doc);
                        }

                        LogManager.write("5");

                        List<Object> cur_comments;
                        List<ObjectId> li;
                        Integer li_size;

                        HashMap<String, Object> cur;
                        List<Map<String,Object>> posts = new ArrayList<>();
                        Document tmpUser;
                        for (Document doc : findIterable) {
                            cur = new HashMap<>();

                            cur.put("_id", doc.getObjectId("_id").toString());
                            cur.put("fitness_center_id", doc.getObjectId("fitness_center_id").toString());
                            cur.put("is_center", doc.getBoolean("is_center"));
                            cur.put("posterId", doc.getObjectId("posterId").toString());
                            cur.put("isMine", doc.getObjectId("posterId").toString().equals(manager.getField(Fitness_Center_Manager.Field.ID).toString()));
                            cur.put("posterName", doc.getString("posterName"));

                            cur.put("likedByMe", true);

                            LogManager.write("6");

                            if (doc.getBoolean("is_center")) {
                                tmpUser = (Document)centers.get(doc.getObjectId("fitness_center_id").toString());
                            } else {
                                tmpUser = (Document)users.get(doc.getObjectId("posterId").toString());
                            }

                            LogManager.write("7");

                            if (tmpUser == null || tmpUser.getObjectId("picture_id") == null ||
                                    tmpUser.getObjectId("picture_id").toString().equals("")) {

                                cur.put("posterPicture", "");
                            } else {
                                cur.put("posterPicture", pictures.get(tmpUser.getObjectId("picture_id").toString()));
                            }

                            LogManager.write("8");

                            cur.put("date", doc.getLong("date"));
                            cur.put("content", doc.getString("content"));
                            cur.put("title", doc.getString("title"));

                            cur.put("type", doc.getString("type"));

                            LogManager.write("9");

                            if (doc.getString("picture") != null && doc.getObjectId("picture_id") != null) {
                                cur.put("picture", doc.getString("picture"));
                                cur.put("picture_id", doc.getObjectId("picture_id").toString());
                            }

                            LogManager.write("10");

                            if (doc.getObjectId("event_id") != null) {
                                cur.put("event_id", doc.getObjectId("event_id").toString());
                                cur.put("start_date", doc.getLong("start_date"));
                                cur.put("end_date", doc.getLong("end_date"));
                            }

                            LogManager.write("11");

                            li = (List<ObjectId>)doc.get("likes");
                            li_size = (li == null ? 0 : li.size());

                            cur.put("nb_likes", li_size);

                            li = (List<ObjectId>)doc.get("comments");
                            li_size = (li == null ? 0 : li.size());

                            cur.put("nb_comments", li_size);

                            cur_comments = new ArrayList<>();
                            if (li != null) {
                                for (ObjectId cur_id : li) {
                                    cur_comments.add(comments.get(cur_id.toString()));
                                }
                            }

                            LogManager.write("12");

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
