package server.api.routes.admin.module;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Administrator;
import model.entities.ElectricProduction;
import model.entities.Module;
import model.entities.ModuleState;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.admin.Protocol;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class CreateModule {
    public CreateModule(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.MODULE.path).handler(routingContext -> {

            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Administrator admin;
            Module module;
            ModuleState module_state;

            try {
                admin = (Administrator) Database.find_entity(Database.Collections.Administrators, Administrator.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(admin.getField(Administrator.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);

                } else if (received.get(Protocol.Field.FITNESS_CENTER_ID.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else if ((!(Boolean)received.get(Protocol.Field.AUTO_GENERATE_UUID.key) &&
                        received.get(Protocol.Field.UUID.key) == null)) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else if (received.get(Protocol.Field.MACHINE_TYPE.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else if (received.get(Protocol.Field.MODULE_STATE_CODE.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else {

                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, protocol.admin.Protocol.Status.GENERIC_OK.code);

                    Long time = System.currentTimeMillis();

                    module = (Module) Database.new_entity(Database.Collections.Modules);

                    module.setField(Module.Field.FITNESS_CENTER_ID, new ObjectId((String)received.get(Protocol.Field.FITNESS_CENTER_ID.key)));

                    if (!(Boolean)received.get(Protocol.Field.AUTO_GENERATE_UUID.key)) {
                        module.setField(Module.Field.UUID, received.get(Protocol.Field.UUID.key));
                    } else {
                        String rand1, rand2, rand3;
                        Random rand = new Random();
                        Integer r;

                        r = rand.nextInt(999) + 1;
                        rand1 = (r < 99 ? "0" + (r < 9 ? "0" + r.toString() : r.toString()) : r.toString());
                        r = rand.nextInt(999) + 1;
                        rand2 = (r < 99 ? "0" + (r < 9 ? "0" + r.toString() : r.toString()) : r.toString());
                        r = rand.nextInt(999) + 1;
                        rand3 = (r < 99 ? "0" + (r < 9 ? "0" + r.toString() : r.toString()) : r.toString());

                        String uuid = rand1 + ":" + rand2 + ":" + rand3;
                        module.setField(Module.Field.UUID, uuid);
                    }

                    module_state = (ModuleState) Database.find_entity(Database.Collections.ModuleStates, ModuleState.Field.CODE, received.get(Protocol.Field.MODULE_STATE_CODE.key));

                    module.setField(Module.Field.SESSION_ID, "");
                    module.setField(Module.Field.MACHINE_TYPE, received.get(Protocol.Field.MACHINE_TYPE.key).toString().toUpperCase());
                    module.setField(Module.Field.NEED_NEW_SESSION_ID, false);
                    module.setField(Module.Field.MODULE_STATE_ID, module_state.getField(ModuleState.Field.ID));
                    module.setField(Module.Field.MODULE_STATE_CODE, received.get(Protocol.Field.MODULE_STATE_CODE.key));

                    module.setField(Module.Field.CREATION_DATE, time);
                    module.setField(Module.Field.UPDATE_DATE, time);

                    module.setField(Module.Field.CREATOR_ADMIN_ID, admin.getField(Administrator.Field.ID));

                    Database.update_entity(Database.Collections.Modules, module);

                    sending.put(Protocol.Field.MODULE_ID.key, module.getField(Module.Field.ID).toString());
                    sending.put(Protocol.Field.UUID.key, module.getField(Module.Field.UUID));
                    sending.put(Protocol.Field.CREATOR_ADMIN_ID.key, admin.getField(Administrator.Field.ID).toString());
                    sending.put(Protocol.Field.CREATOR_ADMIN_NAME.key,
                            admin.getField(Administrator.Field.FIRSTNAME) + " " +
                            admin.getField(Administrator.Field.LASTNAME)
                    );
                }
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
