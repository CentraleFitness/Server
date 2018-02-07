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
import protocol.ResponseObject;

import java.util.Map;

public class Registration {
    public Registration(Router router) {
        router.route(HttpMethod.POST, ProtocolIntranet.Path.REGISTRATION.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Database database = Database.getInstance();

            try {
                if (received.get(ProtocolIntranet.Field.EMAIL.key) == null) {
                    LogManager.write("Missing email key");
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.REG_ERROR_EMAIL.code);
                } else if (received.get(ProtocolIntranet.Field.PASSWORD.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.REG_ERROR_PASSWORD.code);
                    LogManager.write("Missing password key");
                } else if (received.get(ProtocolIntranet.Field.FIRSTNAME.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.REG_ERROR_FIRSTNAME.code);
                    LogManager.write("Missing firstname key");
                } else if (received.get(ProtocolIntranet.Field.LASTNAME.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.REG_ERROR_LASTNAME.code);
                    LogManager.write("Missing lastname key");
                } else if (database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, received.get(ProtocolIntranet.Field.EMAIL.key)) == null) {
                    manager = (Fitness_Center_Manager) database.new_entity(Database.Collections.Fitness_Center_Managers);
                    manager.setField(Fitness_Center_Manager.Field.FIRSTNAME, received.get(ProtocolIntranet.Field.FIRSTNAME.key));
                    manager.setField(Fitness_Center_Manager.Field.LASTNAME, received.get(ProtocolIntranet.Field.LASTNAME.key));
                    manager.setField(Fitness_Center_Manager.Field.PHONE, received.get(ProtocolIntranet.Field.PHONE.key));
                    manager.setField(Fitness_Center_Manager.Field.EMAIL, received.get(ProtocolIntranet.Field.EMAIL.key));
                    manager.setField(Fitness_Center_Manager.Field.PASSWORD_HASH, new PasswordAuthentication().hash(((String) received.get(ProtocolIntranet.Field.PASSWORD.key)).toCharArray()));
                    manager.setField(Fitness_Center_Manager.Field.TOKEN, new Token((String) received.get(ProtocolIntranet.Field.EMAIL.key), (String) received.get(ProtocolIntranet.Field.PASSWORD.key)).generate());
                    database.update_entity(Database.Collections.Fitness_Center_Managers, manager);
                    sending = new ResponseObject(false);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.REG_SUCCESS.code);
                    sending.put(ProtocolIntranet.Field.TOKEN.key, (String) manager.getField(Fitness_Center_Manager.Field.TOKEN));
                } else {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.REG_ERROR_EMAIL_TAKEN.code);
                    LogManager.write("Email already taken");
                }
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
