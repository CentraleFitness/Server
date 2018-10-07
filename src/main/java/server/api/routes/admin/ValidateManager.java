package server.api.routes.admin;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Administrator;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import protocol.ResponseObject;
import protocol.admin.Protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ValidateManager {
    public ValidateManager(Router router) {
        router.route(HttpMethod.PUT, Protocol.Path.MANAGER.path).handler(routingContext -> {

            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Administrator admin;

            try {
                admin = (Administrator) Database.find_entity(Database.Collections.Administrators, Administrator.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(admin.getField(Administrator.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);

                } else if (received.get(Protocol.Field.FITNESS_CENTER_MANAGER_ID.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else if (received.get(Protocol.Field.IS_VALIDATED.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                    Fitness_Center_Manager manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.ID, received.get(Protocol.Field.FITNESS_CENTER_MANAGER_ID.key));

                    if ((Boolean) received.get(Protocol.Field.IS_VALIDATED.key)) {
                        manager.setField(Fitness_Center_Manager.Field.IS_ACTIVE, true);
                        manager.setField(Fitness_Center_Manager.Field.IS_VALIDATED, true);
                    } else {
                        manager.setField(Fitness_Center_Manager.Field.IS_ACTIVE, false);
                        manager.setField(Fitness_Center_Manager.Field.IS_VALIDATED, false);
                    }
                    Database.update_entity(Database.Collections.Fitness_Center_Managers, manager);
                }

            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
