package server.api.routes.admin.manager;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.client.FindIterable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Administrator;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.admin.Protocol;

import java.util.*;

import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.orderBy;

public class GetManagers {
    public GetManagers(Router router) {
        router.route(HttpMethod.GET, Protocol.Path.MANAGER.path).handler(routingContext -> {

            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Administrator admin;

            try {
                admin = (Administrator) Database.find_entity(Database.Collections.Administrators, Administrator.Field.EMAIL, Token.decodeToken(routingContext.request().getParam(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(admin.getField(Administrator.Field.TOKEN), routingContext.request().getParam(Protocol.Field.TOKEN.key))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);

                } else {

                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, protocol.intranet.Protocol.Status.GENERIC_OK.code);

                    @SuppressWarnings("unchecked")
                    FindIterable<Fitness_Center> findIterableCenter = (FindIterable<Fitness_Center>) Database.collections.get(Database.Collections.Fitness_Centers).find();
                    Map<String,Object> centers = new HashMap<>();
                    for (Document doc : findIterableCenter) {
                        centers.put(doc.getObjectId("_id").toString(), doc);
                    }

                    @SuppressWarnings("unchecked")
                    FindIterable<Administrator> findIterableAdmin = (FindIterable<Administrator>) Database.collections.get(Database.Collections.Administrators).find();
                    Map<String,Object> admins = new HashMap<>();
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
                    FindIterable<Fitness_Center_Manager> findIterable = (FindIterable<Fitness_Center_Manager>) Database.collections.get(Database.Collections.Fitness_Center_Managers).find().sort(orderBy(descending(Fitness_Center_Manager.Field.CREATION_DATE.get_key())));
                    List<Map<String,Object>> managers = new ArrayList<>();
                    HashMap<String,Object> cur;
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
                        cur.put("is_principal", doc.getBoolean("is_principal"));


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
