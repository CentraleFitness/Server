package server.api.routes.intranet;

import Tools.LogManager;
import Tools.PasswordAuthentication;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Fitness_Center_Manager;
import protocol.ProtocolIntranet;
import protocol.mobile.ResponseObject;

import java.util.Map;
import java.util.Objects;

public class ManagerUpdatePassword {
    public ManagerUpdatePassword(Router router) {
        router.route(HttpMethod.POST, ProtocolIntranet.Path.MANAGER_UPDATE_PASSWORD.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Database database = Database.getInstance();

            try {
                manager = (Fitness_Center_Manager) database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(ProtocolIntranet.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(ProtocolIntranet.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.AUTH_ERROR_TOKEN.code);
                } else if (!new PasswordAuthentication().authenticate(((String) received.get(ProtocolIntranet.Field.PASSWORD.key)).toCharArray(), (String) manager.getField(Fitness_Center_Manager.Field.PASSWORD_HASH))) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.AUTH_ERROR_CREDENTIALS.code);
                } else {
                    manager.setField(Fitness_Center_Manager.Field.PASSWORD_HASH, new PasswordAuthentication().hash(((String) received.get(ProtocolIntranet.Field.NEW_PASSWORD.key)).toCharArray()));
                    manager.setField(Fitness_Center_Manager.Field.TOKEN, new Token((String) manager.getField(Fitness_Center_Manager.Field.EMAIL), (String) received.get(ProtocolIntranet.Field.NEW_PASSWORD.key)).generate());
                    database.update_entity(Database.Collections.Fitness_Center_Managers, manager);
                    sending = new ResponseObject(false);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_OK.code);
                    sending.put(ProtocolIntranet.Field.TOKEN.key, (String) manager.getField(Fitness_Center_Manager.Field.TOKEN));
                }
            } catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

    }
}
