package server.api.routes.module;

import Tools.LogManager;
import com.google.gson.GsonBuilder;
import com.mongodb.client.model.Filters;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.actions.EndSportSession;
import model.entities.ElectricProduction;
import model.entities.Fitness_Center;
import model.entities.Module;
import model.entities.SportSession;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.module.Protocol;
import static com.mongodb.client.model.Filters.eq;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.mongodb.client.model.Filters.and;

public class ModulePairStop {
    public ModulePairStop(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.MODULE_PAIR_STOP.path).handler(routingContext -> {

            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            label:try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rApiKey = (String) received.get(Protocol.Field.APIKEY.key);
                ArrayList<String> rUUID = (ArrayList) received.get(Protocol.Field.UUID.key);

                if (rApiKey == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.APIKEY.key);
                    break label;
                }
                if (rUUID == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.PRODUCTION.key);
                    break label;
                }
                Fitness_Center fitness_center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.API_KEY, rApiKey);
                if (fitness_center == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("ApiKey not found in database");
                    break label;
                }

                //Stop every pairs
                ArrayList commande = new ArrayList();
                for (int i = 0, j = rUUID.size(); i < j; ++i) {
                    String uuid = rUUID.get(i);
                    if (uuid == null) continue ;
                    Module module = (Module) Database.find_entity(Database.Collections.Modules, Module.Field.UUID, rUUID.get(i));
                    if (module == null) module = (Module) Database.new_entity(Database.Collections.Modules);
                    String sessionID = new ObjectId().toString();
                    module.setField(Module.Field.SESSION_ID, sessionID);
                    Database.update_entity(Database.Collections.Modules, module);
                    SportSession sportSession = (SportSession) Database.find_entity(Database.Collections.SportSessions, SportSession.Field.MODULE_ID, module.getField(Module.Field.ID));
                    EndSportSession.end(sportSession);
                    List setModuleId = new ArrayList();
                    setModuleId.add(Protocol.Command.SET_MODULE_ID.key);
                    setModuleId.add(rUUID.get(i));
                    setModuleId.add(sessionID);
                    commande.add(setModuleId);
                }
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.COMMAND.key, commande);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK);
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
                LogManager.write(e);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
