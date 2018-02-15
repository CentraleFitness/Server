package server.api.routes.intranet;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Fitness_Center_Manager;
import model.entities.Picture;
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.Map;
import java.util.Objects;

public class ManagerGetPicture {
    public ManagerGetPicture(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.MANAGER_GET_PICTURE.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Picture picture;

            try {
                manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    picture = (Picture) Database.find_entity(Database.Collections.Pictures, Picture.Field.ID, manager.getField(Fitness_Center_Manager.Field.PICTURE_ID));

                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                    if (picture == null) {
                        sending.put(Protocol.Field.PICTURE.key, "");
                    } else {
                        sending.put(Protocol.Field.PICTURE.key, picture.getField(Picture.Field.PICTURE));
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
