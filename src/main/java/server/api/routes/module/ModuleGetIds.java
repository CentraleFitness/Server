package server.api.routes.module;

import Tools.LogManager;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Fitness_Center;
import model.entities.Module;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.module.Protocol;
import java.util.Map;

public class ModuleGetIds {
    public ModuleGetIds(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.MODULE_GET_IDS.path).handler(routingContext -> {

            ResponseObject sending = null;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            label:try {
                Map<String, Object> received = routingContext.getBodyAsJson().getMap();
                String rApiKey = (String) received.get(Protocol.Field.APIKEY.key);
                String[] rUUID = (String[]) received.get(Protocol.Field.UUID.key);

                if (rApiKey == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key apiKey");
                    break label;
                }
                if (rUUID == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing key UUID");
                    break label;
                }
                Fitness_Center fitness_center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.API_KEY, rApiKey);
                if (fitness_center == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_KO.code);
                    LogManager.write("Missing UUID not found in database");
                    break label;
                }
                for (String uuid : rUUID) {
                    Module module = (Module) Database.find_entity(Database.Collections.Modules, Module.Field.UUID, uuid);
                    if (module == null) {
                        module = (Module) Database.new_entity(Database.Collections.Modules);
                        module.setField(Module.Field.UUID, rUUID);
                    }
                    String moduleID = new ObjectId().toString();
                    module.setField(Module.Field.SESSION_ID, moduleID);
                    Database.update_entity(Database.Collections.Modules, module);
                    uuid = moduleID;
                }
                sending.put(Protocol.Field.MODULEIDS.key, String.valueOf(rUUID));
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.INTERNAL_SERVER_ERROR.code);
                LogManager.write(e);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
