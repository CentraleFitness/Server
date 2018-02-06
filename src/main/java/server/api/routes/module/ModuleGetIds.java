package server.api.routes.module;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import protocol.ResponseObject;
import protocol.module.Protocol;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class ModuleGetIds {
    public ModuleGetIds(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.MODULE_GET_IDS.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            String rApiKey = (String) received.get(Protocol.Field.APIKEY.key);
            String[] rUUID = (String[]) received.get(Protocol.Field.UUID.key);

            ResponseObject sending = new ResponseObject(false);
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Database database = Database.getInstance();

            try {

            } catch (Exception e) {
                LogManager.write(e);
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
