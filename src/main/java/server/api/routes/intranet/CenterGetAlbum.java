package server.api.routes.intranet;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.client.model.Filters;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import javafx.geometry.Pos;
import model.Database;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import model.entities.Post;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Sorts.descending;
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
                } else {
                    center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_NO_CENTER.code);
                    } else {

                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                        Bson posts_filter = Filters.and(
                                Filters.eq(Post.Field.POSTERID.get_key(), center.getField(Fitness_Center.Field.ID)),
                                Filters.eq(Post.Field.IS_CENTER.get_key(), true),
                                Filters.eq(Post.Field.TYPE.get_key(), "PHOTO")
                        );
                        @SuppressWarnings("unchecked")
                        ArrayList<Document> posts = (ArrayList<Document>) Database.collections.get(Database.Collections.Posts).find(posts_filter).sort(orderBy(ascending(Post.Field.DATE.get_key()))).into(new ArrayList<Document>());

                        sending.put(Protocol.Field.ALBUM.key, posts);

                        /*

                        @SuppressWarnings("unchecked")
                        ArrayList<Fitness_Center.Picture_Describe> album = (ArrayList<Fitness_Center.Picture_Describe>) center.getField(Fitness_Center.Field.ALBUM);
                        sending.put(Protocol.Field.ALBUM.key, album);

                        */
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
