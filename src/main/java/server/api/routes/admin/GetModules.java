package server.api.routes.admin;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Administrator;
import model.entities.Module;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.admin.Protocol;

import java.util.*;

import static com.mongodb.client.model.Indexes.descending;
import static com.mongodb.client.model.Sorts.orderBy;

public class GetModules {
    public GetModules(Router router) {
        router.route(HttpMethod.GET, Protocol.Path.MODULE.path).handler(routingContext -> {

            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Administrator admin;

            try {
                admin = (Administrator) Database.find_entity(Database.Collections.Administrators, Administrator.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(admin.getField(Administrator.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);

                } else {

                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, protocol.intranet.Protocol.Status.GENERIC_OK.code);

                    Bson filter = Filters.and();

                    if (received.get(Protocol.Field.FITNESS_CENTER_ID.key) != null) {

                        filter = Filters.and(
                                Filters.eq(Module.Field.FITNESS_CENTER_ID.get_key(), received.get(Protocol.Field.FITNESS_CENTER_ID.key))
                        );

                    }

                    @SuppressWarnings("unchecked")
                    FindIterable<Module> findIterable = (FindIterable<Module>) Database.collections.get(Database.Collections.Modules).find(filter).sort(orderBy(descending(Module.Field.MODULE_STATE_CODE.get_key())));
                    List<Map<String,Object>> modules = new ArrayList<>();
                    HashMap<String,Object> cur;
                    for (Document doc : findIterable) {
                        cur = new HashMap<>();
                        cur.put("_id", doc.getObjectId("_id").toString());
                        cur.put("UUID", doc.getString("UUID"));
                        cur.put("fitness_center_id", doc.getObjectId("fitness_center_id").toString());
                        cur.put("fitness_center_name", "");
                        cur.put("machine_type", doc.getString("machine_type"));
                        cur.put("module_state_id", doc.getObjectId("module_state_id").toString());
                        cur.put("module_state_code", doc.getInteger("module_state_code"));
                        modules.add(cur);
                    }
                    sending.put(Protocol.Field.MODULES.key, modules);
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
