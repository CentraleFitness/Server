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

public class AuthenticationWithCredentials {
    public AuthenticationWithCredentials(Router router) {
        router.route(HttpMethod.POST, ProtocolIntranet.Path.AUTHENTICATION.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Database database = Database.getInstance();

            try {
                if ((manager = (Fitness_Center_Manager) database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, received.get(ProtocolIntranet.Field.EMAIL.key))) != null) {
                    if (new PasswordAuthentication().authenticate(((String) received.get(ProtocolIntranet.Field.PASSWORD.key)).toCharArray(), (String) manager.getField(Fitness_Center_Manager.Field.PASSWORD_HASH))) {
                        sending = new ResponseObject(false);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.AUTH_SUCCESS.code);
                        manager.setField(Fitness_Center_Manager.Field.TOKEN, new Token((String) received.get(ProtocolIntranet.Field.EMAIL.key), (String) received.get(ProtocolIntranet.Field.PASSWORD.key)).generate());
                        sending.put(ProtocolIntranet.Field.TOKEN.key, (String) manager.getField(Fitness_Center_Manager.Field.TOKEN));
                        database.update_entity(Database.Collections.Fitness_Center_Managers, manager);
                    } else {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.AUTH_ERROR_CREDENTIALS.code);
                    }
                } else {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.AUTH_ERROR_CREDENTIALS.code);
                }
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.AUTH_ERROR_CREDENTIALS.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
