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

                        post.setField(Post.Field.POSTERID, new ObjectId(center.getField(Fitness_Center.Field.ID).toString()));
                        post.setField(Post.Field.POSTERNAME, center.getField(Fitness_Center.Field.NAME));
                        post.setField(Post.Field.IS_CENTER, true);
                        post.setField(Post.Field.TYPE, "PUBLICATION");
                        post.setField(Post.Field.DATE, System.currentTimeMillis());
                        post.setField(Post.Field.CONTENT, text);

                        Database.update_entity(Database.Collections.Posts, post);

                        sending.put(Protocol.Field.PUBLICATION_ID.key, post.getField(Post.Field.ID).toString());

                        /*
                        @SuppressWarnings("unchecked")
                        ArrayList<Fitness_Center.Publication> publications = (ArrayList<Fitness_Center.Publication>) center.getField(Fitness_Center.Field.PUBLICATIONS);
                        Fitness_Center.Publication publication = new Fitness_Center.Publication();
                        publication.setField(Fitness_Center.Publication.Field.TEXT, text);
                        publication.setField(Fitness_Center.Publication.Field.CREATION_DATE, System.currentTimeMillis());
                        publications.add(publication);
                        //center.setField(Fitness_Center.Field.PUBLICATIONS, publications);
                        Database.update_entity(Database.Collections.Fitness_Centers, center);
                        */
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
