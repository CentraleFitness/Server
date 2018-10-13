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
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.admin.Protocol;

import java.util.*;

import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

public class GetManagers {
    public GetManagers(Router router) {
        router.route(HttpMethod.GET, Protocol.Path.MANAGER.path).handler(routingContext -> {

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
                                Filters.eq(Fitness_Center_Manager.Field.FITNESS_CENTER_ID.get_key(), received.get(Protocol.Field.FITNESS_CENTER_ID.key))
                        );

                    }

                    @SuppressWarnings("unchecked")
                    FindIterable<Fitness_Center> findIterableCenter = (FindIterable<Fitness_Center>) Database.collections.get(Database.Collections.Fitness_Centers).find(filter);
                    Map<String,Object> centers = new HashMap<>();
                    for (Document doc : findIterableCenter) {
                        centers.put(doc.getObjectId("_id").toString(), doc);
                    }

                    @SuppressWarnings("unchecked")
                    FindIterable<Fitness_Center_Manager> findIterable = (FindIterable<Fitness_Center_Manager>) Database.collections.get(Database.Collections.Fitness_Center_Managers).find(filter).sort(orderBy(ascending(Fitness_Center_Manager.Field.LASTNAME.get_key())));
                    List<Map<String,Object>> managers = new ArrayList<>();
                    HashMap<String,Object> cur;
                    for (Document doc : findIterable) {
                        cur = new HashMap<>();
                        cur.put("_id", doc.getObjectId("_id").toString());
                        cur.put("first_name", doc.getString("first_name"));
                        cur.put("last_name", doc.getString("last_name"));
                        cur.put("email_address", doc.getString("email_address"));
                        cur.put("phone_number", doc.getString("phone_number"));
                        cur.put("is_active", doc.getString("is_active"));
                        cur.put("is_validated", doc.getString("is_validated"));
                        cur.put("creation_date", doc.getLong("creation_date"));
                        cur.put("update_date", doc.getLong("update_date"));

                        //TODO ECLATER ???
                        cur.put("fitness_center", centers.get(doc.getObjectId("fitness_center_id").toString()));

                        managers.add(cur);
                    }
                    sending.put(Protocol.Field.MANAGERS.key, managers);
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
