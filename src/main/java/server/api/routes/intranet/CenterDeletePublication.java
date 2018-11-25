package server.api.routes.intranet;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import model.entities.Post;
import org.bson.types.ObjectId;
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class CenterDeletePublication {
    public CenterDeletePublication(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.CENTER_DELETE_PUBLICATION.path).handler(routingContext -> {
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

                } else if (received.get(Protocol.Field.PUBLICATION_ID.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);
                } else {
                    center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_NO_CENTER.code);
                    } else {

                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                        Post post_to_delete;

                        if (received.get(Protocol.Field.COMMENT_ID.key) != null) {

                            Post post = (Post) Database.find_entity(Database.Collections.Posts, Post.Field.ID, new ObjectId(received.get(Protocol.Field.PUBLICATION_ID.key).toString()));

                            ArrayList<ObjectId> comments = (ArrayList<ObjectId>) post.getField(Post.Field.COMMENTS);

                            if (comments == null) {
                                comments = new ArrayList<>();
                            }

                            int index = comments.indexOf(new ObjectId(received.get(Protocol.Field.COMMENT_ID.key).toString()));

                            if (index != -1) {
                                comments.remove(index);
                                post.setField(Post.Field.COMMENTS, comments);
                                Database.update_entity(Database.Collections.Posts, post);
                            }

                            //Database.delete_entity(Database.Collections.Posts, Post.Field.ID, new ObjectId(received.get(Protocol.Field.COMMENT_ID.key).toString()));

                            post_to_delete = (Post) Database.find_entity(Database.Collections.Posts, Post.Field.ID, new ObjectId(received.get(Protocol.Field.COMMENT_ID.key).toString()));

                            post_to_delete.setField(Post.Field.IS_DELETED, true);

                            Database.update_entity(Database.Collections.Posts, post_to_delete);

                        } else {

                            //Database.delete_entity(Database.Collections.Posts, Post.Field.ID, new ObjectId(received.get(Protocol.Field.PUBLICATION_ID.key).toString()));

                            post_to_delete = (Post) Database.find_entity(Database.Collections.Posts, Post.Field.ID, new ObjectId(received.get(Protocol.Field.PUBLICATION_ID.key).toString()));

                            post_to_delete.setField(Post.Field.IS_DELETED, true);

                            Database.update_entity(Database.Collections.Posts, post_to_delete);
                        }
                    }
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
