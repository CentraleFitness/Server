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
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class CenterDeletePictureFromAlbum {
    public CenterDeletePictureFromAlbum(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.CENTER_DECREASE_ALBUM.path).handler(routingContext -> {
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
                } else if (received.get(Protocol.Field.PICTURE_ID.key) == null) {
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

                        Database.delete_entity(Database.Collections.Posts, Post.Field.PICTURE_ID, new ObjectId(received.get(Protocol.Field.PICTURE_ID.key).toString()));

                        /*
                        @SuppressWarnings("unchecked")
                        ArrayList<Fitness_Center.Picture_Describe> album = (ArrayList<Fitness_Center.Picture_Describe>) center.getField(Fitness_Center.Field.ALBUM);
                        Document cur;
                        for(Iterator<Fitness_Center.Picture_Describe> i = album.iterator(); i.hasNext();) {
                            cur = i.next();
                            if (cur.get(Fitness_Center.Picture_Describe.Field.PICTURE_ID.get_key()).toString().equals(received.get(Protocol.Field.PICTURE_ID.key).toString())) {
                                i.remove();
                                break;
                            }
                        }
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
