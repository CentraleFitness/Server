package server.api.routes.intranet.picture;

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
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import model.entities.Post;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.*;

import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

public class CenterGetAlbum {
    public CenterGetAlbum(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.CENTER_GET_ALBUM.path).handler(routingContext -> {
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
                                Filters.eq(Post.Field.IS_CENTER.get_key(), true),
                                Filters.eq(Post.Field.TYPE.get_key(), "PHOTO"),
                                Filters.or(
                                    Filters.eq(Post.Field.IS_DELETED.get_key(), false),
                                    Filters.eq(Post.Field.IS_DELETED.get_key(), null)
                                )
                        );

                        @SuppressWarnings("unchecked")
                        FindIterable<Post> findIterable = (FindIterable<Post>) Database.collections.get(Database.Collections.Posts).find(posts_filter).sort(orderBy(ascending(Post.Field.DATE.get_key())));
                        List<Map<String,Object>> albums = new ArrayList<>();
                        HashMap<String,Object> cur;
                        for (Document doc : findIterable) {
                            cur = new HashMap<>();

                            cur.put("_id", doc.getObjectId("_id").toString());
                            cur.put("posterId", doc.getObjectId("posterId").toString());
                            cur.put("posterName", doc.getString("posterName"));

                            cur.put("creation_date", doc.getLong("date"));

                            cur.put("date", doc.getLong("date"));
                            cur.put("content", doc.getString("content"));
                            cur.put("title", doc.getString("title"));

                            cur.put("picture", doc.getString("picture"));
                            cur.put("picture_id", doc.getObjectId("picture_id").toString());

                            albums.add(cur);
                        }

                        sending.put(Protocol.Field.ALBUM.key, albums);
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
