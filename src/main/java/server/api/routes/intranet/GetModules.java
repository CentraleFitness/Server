package server.api.routes.intranet;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.client.model.Filters;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.*;
import model.entities.Module;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.util.*;

import static com.mongodb.client.model.Indexes.descending;
import static com.mongodb.client.model.Sorts.orderBy;

public class GetModules {
    public GetModules(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.GET_MODULES.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;

            try {
                manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else if (!((Boolean)manager.getField(Fitness_Center_Manager.Field.IS_ACTIVE))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_ACCOUNT_INACTIVE.code);

                } else {
                    Fitness_Center center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_NO_CENTER.code);
                    } else {
                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                        Bson filter = Filters.and(
                                Filters.eq(Event.Field.FITNESS_CENTER_ID.get_key(), center.getField(Fitness_Center.Field.ID))
                        );

                        @SuppressWarnings("unchecked")
                        ArrayList<Document> findIterable = (ArrayList<Document>) Database.collections.get(Database.Collections.Modules).find(filter).sort(orderBy(descending(Module.Field.MODULE_STATE_CODE.get_key()))).into(new ArrayList<Document>());

                        List<Map<String,Object>> modules = new ArrayList<>();
                        HashMap<String,Object> cur;
                        for (Document doc : findIterable) {
                            cur = new HashMap<>();
                            cur.put("_id", doc.getObjectId("_id").toString());
                            cur.put("UUID", doc.getString("UUID"));
                            cur.put("machine_type", doc.getString("machine_type"));
                            cur.put("module_state_id", doc.getObjectId("module_state_id").toString());
                            cur.put("module_state_code", doc.getInteger("module_state_code"));
                            modules.add(cur);
                        }

                        sending.put(Protocol.Field.MODULES.key, modules);
                    }
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdSerializer()).create().toJson(sending));
        });

    }
}
