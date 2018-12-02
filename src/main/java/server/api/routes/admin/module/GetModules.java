package server.api.routes.admin.module;

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

                    LogManager.write("1");

                    @SuppressWarnings("unchecked")
                    FindIterable<Administrator> findIterableAdmin = (FindIterable<Administrator>) Database.collections.get(Database.Collections.Administrators).find();
                    Map<String,Object> admins = new HashMap<>();
                    for (Document doc : findIterableAdmin) {
                        admins.put(doc.getObjectId("_id").toString(), doc.getString("first_name") + " " + doc.getString("last_name"));
                    }

                    LogManager.write("2");

                    @SuppressWarnings("unchecked")
                    FindIterable<Module> findIterable = (FindIterable<Module>) Database.collections.get(Database.Collections.Modules).find().sort(orderBy(descending(Module.Field.MODULE_STATE_CODE.get_key())));
                    List<Map<String,Object>> modules = new ArrayList<>();
                    HashMap<String,Object> cur;
                    for (Document doc : findIterable) {
                        cur = new HashMap<>();
                        cur.put("_id", doc.getObjectId("_id").toString());
                        cur.put("UUID", doc.getString("UUID"));
                        LogManager.write("3");
                        if (doc.getObjectId("fitness_center_id") != null) {
                            cur.put("fitness_center_id", doc.getObjectId("fitness_center_id").toString());
                        } else {
                            cur.put("fitness_center_id", "");
                        }
                        LogManager.write("4");
                        cur.put("machine_type", doc.getString("machine_type"));
                        LogManager.write("5");
                        if (doc.getObjectId("module_state_id") != null) {
                            cur.put("module_state_id", doc.getObjectId("module_state_id").toString());
                        } else {
                            cur.put("module_state_id", "");
                        }
                        LogManager.write("6");
                        cur.put("module_state_code", doc.getInteger("module_state_code"));
                        LogManager.write("7");
                        cur.put("creation_date", doc.getLong("creation_date"));
                        LogManager.write("8");
                        cur.put("update_date", doc.getLong("update_date"));
                        LogManager.write("9");
                        if (doc.getObjectId("creator_admin_id") != null) {
                            cur.put("creator_admin_id", doc.getObjectId("creator_admin_id").toString());
                        } else {
                            cur.put("creator_admin_id", "");
                        }
                        LogManager.write("10");
                        if (doc.getObjectId("creator_admin_id") != null &&
                                admins.containsKey(doc.getObjectId("creator_admin_id").toString())) {
                            LogManager.write("11");
                            cur.put("creator_admin_name", admins.get(doc.getObjectId("creator_admin_id").toString()));
                        }

                        LogManager.write("12");
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
