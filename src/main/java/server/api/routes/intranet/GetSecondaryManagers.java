package server.api.routes.intranet;

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
import protocol.intranet.Protocol;

import java.util.*;

import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.orderBy;

public class GetSecondaryManagers {
    public GetSecondaryManagers(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.GET_SECONDARY_MANAGERS.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Fitness_Center center;

            try {
                manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else if (!((Boolean)manager.getField(Fitness_Center_Manager.Field.IS_ACTIVE))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_ACCOUNT_INACTIVE.code);

                } else {
                    center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_NO_CENTER.code);
                    } else {
                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                        Bson filter = Filters.and(
                                Filters.eq(Fitness_Center_Manager.Field.FITNESS_CENTER_ID.get_key(), manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID)),
                                Filters.eq(Fitness_Center_Manager.Field.IS_PRINCIPAL.get_key(), false),
                                Filters.eq(Fitness_Center_Manager.Field.IS_REFUSED.get_key(), false)
                        );

                        @SuppressWarnings("unchecked")
                        FindIterable<Administrator> findIterableAdmin = (FindIterable<Administrator>) Database.collections.get(Database.Collections.Administrators).find();
                        Map<String, Object> admins = new HashMap<>();
                        for (Document doc : findIterableAdmin) {
                            admins.put(doc.getObjectId("_id").toString(), doc.getString("first_name") + " " + doc.getString("last_name"));
                        }

                        @SuppressWarnings("unchecked")
                        FindIterable<Fitness_Center_Manager> findIterableManagers = (FindIterable<Fitness_Center_Manager>) Database.collections.get(Database.Collections.Fitness_Center_Managers).find();
                        Map<String, Object> managers_principals = new HashMap<>();
                        for (Document doc : findIterableManagers) {
                            managers_principals.put(doc.getObjectId("_id").toString(), doc.getString("first_name") + " " + doc.getString("last_name"));
                        }

                        @SuppressWarnings("unchecked")
                        FindIterable<Fitness_Center_Manager> findIterable = (FindIterable<Fitness_Center_Manager>) Database.collections.get(Database.Collections.Fitness_Center_Managers).find(filter).sort(orderBy(descending(Fitness_Center_Manager.Field.CREATION_DATE.get_key())));
                        List<Map<String, Object>> managers = new ArrayList<>();
                        HashMap<String, Object> cur;
                        for (Document doc : findIterable) {
                            cur = new HashMap<>();
                            cur.put("_id", doc.getObjectId("_id").toString());
                            cur.put("first_name", doc.getString("first_name"));
                            cur.put("last_name", doc.getString("last_name"));
                            cur.put("email_address", doc.getString("email_address"));
                            cur.put("phone_number", doc.getString("phone_number"));
                            cur.put("is_active", doc.getBoolean("is_active"));
                            cur.put("last_update_activity", doc.getLong("last_update_activity"));
                            cur.put("last_update_admin_id", doc.getObjectId("last_update_admin_id"));
                            cur.put("last_update_admin_is_manager", doc.getBoolean("last_update_admin_is_manager"));
                            cur.put("is_validated", doc.getBoolean("is_validated"));
                            cur.put("is_refused", doc.getBoolean("is_refused"));
                            cur.put("validation_date", doc.getLong("validation_date"));
                            cur.put("validator_admin_id", doc.getObjectId("validator_admin_id"));
                            cur.put("validator_admin_is_manager", doc.getBoolean("validator_admin_is_manager"));
                            cur.put("creation_date", doc.getLong("creation_date"));


                            if (!doc.getBoolean("validator_admin_is_manager") &&
                                    doc.getObjectId("validator_admin_id") != null &&
                                    admins.containsKey(doc.getObjectId("validator_admin_id").toString())) {

                                cur.put("validator_admin_name", admins.get(doc.getObjectId("validator_admin_id").toString()));

                            } else if (doc.getBoolean("validator_admin_is_manager") &&
                                    doc.getObjectId("validator_admin_id") != null &&
                                    managers_principals.containsKey(doc.getObjectId("validator_admin_id").toString())) {

                                cur.put("validator_admin_name", managers_principals.get(doc.getObjectId("validator_admin_id").toString()));
                            }

                            if (!doc.getBoolean("last_update_admin_is_manager") &&
                                    doc.getObjectId("last_update_admin_id") != null &&
                                    admins.containsKey(doc.getObjectId("last_update_admin_id").toString())) {

                                cur.put("last_update_admin_name", admins.get(doc.getObjectId("last_update_admin_id").toString()));

                            } else if (doc.getBoolean("last_update_admin_is_manager") &&
                                    doc.getObjectId("last_update_admin_id") != null &&
                                    managers_principals.containsKey(doc.getObjectId("last_update_admin_id").toString())) {

                                cur.put("last_update_admin_name", managers_principals.get(doc.getObjectId("last_update_admin_id").toString()));
                            }

                            managers.add(cur);
                        }
                        sending.put(protocol.admin.Protocol.Field.MANAGERS.key, managers);
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
