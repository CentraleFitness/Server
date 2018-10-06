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
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.Map;

public class Registration {
    public Registration(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.MANAGER_REGISTER.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;

            try {
                if (received.get(Protocol.Field.EMAIL.key) == null) {
                    LogManager.write("Missing email key");
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_EMAIL.code);
                } else if (received.get(Protocol.Field.PASSWORD.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_PASSWORD.code);
                    LogManager.write("Missing password key");
                } else if (received.get(Protocol.Field.FIRSTNAME.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_FIRSTNAME.code);
                    LogManager.write("Missing firstname key");
                } else if (received.get(Protocol.Field.LASTNAME.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_LASTNAME.code);
                    LogManager.write("Missing lastname key");
                } else if (Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, received.get(Protocol.Field.EMAIL.key)) == null) {

                    Long time = System.currentTimeMillis();

                    manager = (Fitness_Center_Manager) Database.new_entity(Database.Collections.Fitness_Center_Managers);
                    manager.setField(Fitness_Center_Manager.Field.FIRSTNAME, received.get(Protocol.Field.FIRSTNAME.key));
                    manager.setField(Fitness_Center_Manager.Field.LASTNAME, received.get(Protocol.Field.LASTNAME.key));
                    manager.setField(Fitness_Center_Manager.Field.PHONE, received.get(Protocol.Field.PHONE.key));
                    manager.setField(Fitness_Center_Manager.Field.EMAIL, received.get(Protocol.Field.EMAIL.key));
                    manager.setField(Fitness_Center_Manager.Field.PASSWORD_HASH, new PasswordAuthentication().hash(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray()));
                    manager.setField(Fitness_Center_Manager.Field.TOKEN, new Token((String) received.get(Protocol.Field.EMAIL.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                    manager.setField(Fitness_Center_Manager.Field.CREATION_DATE, time);

                    Database.update_entity(Database.Collections.Fitness_Center_Managers, manager);
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_SUCCESS.code);
                    sending.put(Protocol.Field.TOKEN.key, manager.getField(Fitness_Center_Manager.Field.TOKEN));
                } else {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_EMAIL_TAKEN.code);
                    LogManager.write("Email already taken");
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
