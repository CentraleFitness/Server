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
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.Map;
import java.util.Objects;

public class ManagerUpdateProfile {
    public ManagerUpdateProfile(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.MANAGER_UPDATE_PROFILE.path).handler(routingContext -> {
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
                        if (received.get(Protocol.Field.FIRSTNAME.key) == null) {
                            sending = new ResponseObject(true);
                            sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_ERROR_FIRSTNAME.code);
                        } else if (received.get(Protocol.Field.LASTNAME.key) == null) {
                            sending = new ResponseObject(true);
                            sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_ERROR_LASTNAME.code);
                        } else if (received.get(Protocol.Field.PHONE.key) == null) {
                            sending = new ResponseObject(true);
                            sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_ERROR_PHONE.code);
                        } else {
                            sending = new ResponseObject(false);
                            sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                            manager.setField(Fitness_Center_Manager.Field.FIRSTNAME, received.get(Protocol.Field.FIRSTNAME.key));
                            manager.setField(Fitness_Center_Manager.Field.LASTNAME, received.get(Protocol.Field.LASTNAME.key));
                            manager.setField(Fitness_Center_Manager.Field.PHONE, received.get(Protocol.Field.PHONE.key));
                            Database.update_entity(Database.Collections.Fitness_Center_Managers, manager);
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
