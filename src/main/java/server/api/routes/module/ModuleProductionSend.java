package server.api.routes.module;

import Tools.LogManager;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.ElectricProduction;
import model.entities.Fitness_Center;
import model.entities.Module;
import model.entities.SportSession;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.module.Protocol;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class ModuleProductionSend {
    public ModuleProductionSend(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.MODULE_PRODUCTION_SEND.path).handler(routingContext -> {

            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            label:try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rApiKey = (String) received.get(Protocol.Field.APIKEY.key);
                Map<String, Double> rProduction = (Map) received.get(Protocol.Field.PRODUCTION.key);

                if (rApiKey == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key " + Protocol.Field.APIKEY.key);
                    break label;
                }
                if (rProduction == null) {
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

                ArrayList commande = new ArrayList();
                Map commande_setModuleIds = new TreeMap();
                ArrayList commande_setModuleIds_params = new ArrayList();
                commande_setModuleIds.put(Protocol.Field.COMMAND_NAME.key, Protocol.Command.SET_MODULE_ID.key);
                commande_setModuleIds.put(Protocol.Field.COMMAND_PARAMS.key, commande_setModuleIds_params);
                commande.add(commande_setModuleIds);

                for (Map.Entry entry : rProduction.entrySet()) {
                    String uuid = (String) entry.getKey();
                    Double production;
                    try {
                        production = (Double) entry.getValue();
                    } catch (ClassCastException cce) {
                        production = ((Integer) entry.getValue()).doubleValue();
                    }

                    Module module = (Module) Database.find_entity(Database.Collections.Modules, Module.Field.UUID, uuid);
                    if (module == null) continue ;
                    ObjectId module_id = (ObjectId) module.getField(Module.Field.ID);

                    SportSession sportSession = (SportSession) Database.find_entity(Database.Collections.SportSessions, SportSession.Field.MODULE_ID, module_id);
                    Boolean change_session_id = false;
                    if ((sportSession == null) && change_session_id == true) {
                        String sessionID = new ObjectId().toString();
                        Map param = new TreeMap();
                        param.put(Protocol.Field.UUID.key, uuid);
                        param.put(Protocol.Field.SESSION_ID.key, sessionID);
                        commande_setModuleIds_params.add(param);
                        module.setField(Module.Field.SESSION_ID, sessionID);
                        Database.update_entity(Database.Collections.Modules, module);
                    }
                    if (sportSession != null) {
                        ArrayList sportSessionProduction = (ArrayList) sportSession.getField(SportSession.Field.PRODUCTION);
                        sportSessionProduction.add(production);
                        Database.update_entity(Database.Collections.SportSessions, sportSession);
                    }
                }
                sending = new ResponseObject(false);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK);
                if (commande_setModuleIds_params.size() > 0)
                    sending.put(Protocol.Field.COMMAND.key, commande);
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
                LogManager.write(e);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
