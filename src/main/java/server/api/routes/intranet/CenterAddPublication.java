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
import model.entities.Picture;
import model.entities.Post;
import org.bson.types.ObjectId;
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class CenterAddPublication {
    public CenterAddPublication(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.CENTER_ADD_PUBLICATION.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Fitness_Center center;
            String text;

            try {
                manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else if (!((Boolean)manager.getField(Fitness_Center_Manager.Field.IS_ACTIVE))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_ACCOUNT_INACTIVE.code);

                } else if ((text = (String) received.get(Protocol.Field.TEXT.key)) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);
                }
                else {
                    center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_NO_CENTER.code);
                    } else {
                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                        Post post = (Post) Database.new_entity(Database.Collections.Posts);

                        post.setField(Post.Field.POSTERID, new ObjectId(manager.getField(Fitness_Center_Manager.Field.ID).toString()));
                        post.setField(Post.Field.POSTERNAME, center.getField(Fitness_Center.Field.NAME));
                        post.setField(Post.Field.FITNESS_CENTERT_ID, center.getField(Fitness_Center.Field.ID));
                        post.setField(Post.Field.IS_CENTER, true);
                        post.setField(Post.Field.TYPE, "PUBLICATION");
                        post.setField(Post.Field.DATE, System.currentTimeMillis());
                        post.setField(Post.Field.CONTENT, text);

                        Database.update_entity(Database.Collections.Posts, post);

                        sending.put(Protocol.Field.PUBLICATION_ID.key, post.getField(Post.Field.ID).toString());
                        sending.put(Protocol.Field.POSTER_NAME.key, center.getField(Fitness_Center.Field.NAME));

                        String picture = "";
                        if (center.getField(Fitness_Center.Field.PICTURE_ID) != null &&
                                !center.getField(Fitness_Center.Field.PICTURE_ID).equals("")) {

                            Picture pic = (Picture)Database.find_entity(Database.Collections.Pictures, Picture.Field.ID, center.getField(Fitness_Center.Field.PICTURE_ID));
                            picture = (String)pic.getField(Picture.Field.PICTURE);
                        }

                        sending.put(Protocol.Field.POSTER_PICTURE.key, picture);
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
