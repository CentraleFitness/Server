package server.api.routes.intranet.module;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.*;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.util.Map;
import java.util.Objects;

public class SetModuleReceived {
    public SetModuleReceived(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.SET_MODULE_RECEIVED.path).handler(routingContext -> {
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

                } else if (received.get(Protocol.Field.MODULE_ID.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else if (received.get(Protocol.Field.IS_RECEIVED.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else {
                    Fitness_Center center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_NO_CENTER.code);
                    } else {
                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                        ModuleState state;
                        if ((Boolean)received.get(Protocol.Field.IS_RECEIVED.key)) {
                            state = (ModuleState) Database.find_entity(Database.Collections.ModuleStates, ModuleState.Field.CODE, 1);
                        } else {
                            state = (ModuleState) Database.find_entity(Database.Collections.ModuleStates, ModuleState.Field.CODE, 0);
                        }

                        model.entities.Module module = (model.entities.Module) Database.find_entity(Database.Collections.Modules, model.entities.Module.Field.ID, new ObjectId((String)received.get(Protocol.Field.MODULE_ID.key)));

                        if (((Boolean)received.get(Protocol.Field.IS_RECEIVED.key) &&
                                (Integer) module.getField(model.entities.Module.Field.MODULE_STATE_CODE) == 1) ||
                                (!(Boolean)received.get(Protocol.Field.IS_RECEIVED.key) &&
                                        (Integer) module.getField(model.entities.Module.Field.MODULE_STATE_CODE) == 0)) {

                            module.setField(model.entities.Module.Field.MODULE_STATE_ID, state.getField(ModuleState.Field.ID));
                            module.setField(model.entities.Module.Field.MODULE_STATE_CODE, state.getField(ModuleState.Field.CODE));

                            Database.update_entity(Database.Collections.Modules, module);

                            sending.put(Protocol.Field.MODULE_STATE_ID.key, state.getField(ModuleState.Field.ID).toString());
                            sending.put(Protocol.Field.MODULE_STATE_CODE.key, state.getField(ModuleState.Field.CODE));
                        }
                    }
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

    }
}
