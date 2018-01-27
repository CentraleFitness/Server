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
import protocol.ProtocolIntranet;
import protocol.mobile.ResponseObject;

import java.util.Map;
import java.util.Objects;

public class CenterGetPicture {
    public CenterGetPicture(Router router) {
        router.route(HttpMethod.POST, ProtocolIntranet.Path.CENTER_GET_PICTURE.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Fitness_Center center;
            Picture picture;
            Database database = Database.getInstance();

            try {
                manager = (Fitness_Center_Manager) database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(ProtocolIntranet.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(ProtocolIntranet.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    center = (Fitness_Center) database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.MGR_ERROR_NO_CENTER.code);
                    } else {
                        picture = (Picture) database.find_entity(Database.Collections.Pictures, Picture.Field.ID, center.getField(Fitness_Center.Field.PICTURE_ID));

                        sending = new ResponseObject(false);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_OK.code);

                        if (picture == null) {
                            sending.put(ProtocolIntranet.Field.PICTURE.key, "");
                        } else {
                            sending.put(ProtocolIntranet.Field.PICTURE.key, (String) picture.getField(Picture.Field.PICTURE));
                        }
                    }
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
