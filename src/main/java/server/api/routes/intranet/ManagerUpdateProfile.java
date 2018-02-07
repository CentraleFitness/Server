package server.api.routes.intranet;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Fitness_Center_Manager;
import protocol.ProtocolIntranet;
import protocol.ResponseObject;

import java.util.Map;
import java.util.Objects;

public class ManagerUpdateProfile {
    public ManagerUpdateProfile(Router router) {
        router.route(HttpMethod.POST, ProtocolIntranet.Path.MANAGER_UPDATE_PROFILE.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Database database = Database.getInstance();

            try {
                manager = (Fitness_Center_Manager) database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(ProtocolIntranet.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(ProtocolIntranet.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    if (received.get(ProtocolIntranet.Field.FIRSTNAME.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.MGR_ERROR_ERROR_FIRSTNAME.code);
                    } else if (received.get(ProtocolIntranet.Field.LASTNAME.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.MGR_ERROR_ERROR_LASTNAME.code);
                    } else if (received.get(ProtocolIntranet.Field.PHONE.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.MGR_ERROR_ERROR_PHONE.code);
                    } else {
                        sending = new ResponseObject(false);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_OK.code);
                        manager.setField(Fitness_Center_Manager.Field.FIRSTNAME, received.get(ProtocolIntranet.Field.FIRSTNAME.key));
                        manager.setField(Fitness_Center_Manager.Field.LASTNAME, received.get(ProtocolIntranet.Field.LASTNAME.key));
                        manager.setField(Fitness_Center_Manager.Field.PHONE, received.get(ProtocolIntranet.Field.PHONE.key));
                        database.update_entity(Database.Collections.Fitness_Center_Managers, manager);
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
