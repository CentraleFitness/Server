package server.api.routes.admin;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Administrator;
import model.entities.Fitness_Center_Manager;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.admin.Protocol;

import java.util.Map;
import java.util.Objects;

public class UndoRefuseManager {
    public UndoRefuseManager(Router router) {
        router.route(HttpMethod.PUT, Protocol.Path.MANAGER_UNDO_REFUSE.path).handler(routingContext -> {

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

                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                    Fitness_Center_Manager manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.ID, new ObjectId((String)received.get(Protocol.Field.FITNESS_CENTER_MANAGER_ID.key)));

                    if (!((Boolean) manager.getField(Fitness_Center_Manager.Field.IS_VALIDATED)) &&
                            ((Boolean) manager.getField(Fitness_Center_Manager.Field.IS_REFUSED))) {

                        manager.setField(Fitness_Center_Manager.Field.IS_ACTIVE, false);
                        manager.setField(Fitness_Center_Manager.Field.IS_VALIDATED, false);
                        manager.setField(Fitness_Center_Manager.Field.IS_REFUSED, false);

                        manager.setField(Fitness_Center_Manager.Field.LAST_UPDATE_ACTIVITY, 0L);
                        manager.setField(Fitness_Center_Manager.Field.LAST_UPDATE_ADMIN_ID, null);

                        manager.setField(Fitness_Center_Manager.Field.VALIDATION_DATE, 0L);
                        manager.setField(Fitness_Center_Manager.Field.VALIDATOR_ADMIN_ID, null);

                        Database.update_entity(Database.Collections.Fitness_Center_Managers, manager);
                    }
                }

            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdSerializer()).create().toJson(sending));
        });
    }
}
