package server.api.routes.intranet;

import Tools.LogManager;
import Tools.PasswordAuthentication;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.util.ArrayList;
import java.util.Map;

public class RegisterManagerAndCenter {
    public RegisterManagerAndCenter(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.REGISTRATION.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager = null;
            Fitness_Center center = null;

            try {
                if (received.get(Protocol.Field.EMAIL.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_EMAIL.code);
                } else if (received.get(Protocol.Field.PASSWORD.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_PASSWORD.code);
                } else if (received.get(Protocol.Field.FIRSTNAME.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_FIRSTNAME.code);
                } else if (received.get(Protocol.Field.LASTNAME.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_LASTNAME.code);
                } else if (Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, received.get(Protocol.Field.EMAIL.key)) == null) {

                    if (received.get(Protocol.Field.NAME.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_ERROR_NAME.code);
                    } else if (received.get(Protocol.Field.DESCRIPTION.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_ERROR_DESCRIPTION.code);
                    } else if (received.get(Protocol.Field.ADDRESS.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_ERROR_ADDRESS.code);
                    } else if (received.get(Protocol.Field.ZIP_CODE.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_ERROR_ZIP_CODE.code);
                    } else if (received.get(Protocol.Field.CITY.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_ERROR_CITY.code);
                    } else if (Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.NAME, received.get(Protocol.Field.NAME.key)) == null) {

                        manager = (Fitness_Center_Manager) Database.new_entity(Database.Collections.Fitness_Center_Managers);
                        manager.setField(Fitness_Center_Manager.Field.FIRSTNAME, received.get(Protocol.Field.FIRSTNAME.key));
                        manager.setField(Fitness_Center_Manager.Field.LASTNAME, received.get(Protocol.Field.LASTNAME.key));
                        manager.setField(Fitness_Center_Manager.Field.PHONE, received.get(Protocol.Field.PHONE.key));
                        manager.setField(Fitness_Center_Manager.Field.EMAIL, received.get(Protocol.Field.EMAIL.key));
                        manager.setField(Fitness_Center_Manager.Field.PASSWORD_HASH, new PasswordAuthentication().hash(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray()));
                        manager.setField(Fitness_Center_Manager.Field.TOKEN, new Token((String) received.get(Protocol.Field.EMAIL.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());

                        center = (Fitness_Center) Database.new_entity(Database.Collections.Fitness_Centers);
                        center.setField(Fitness_Center.Field.NAME, received.get(Protocol.Field.NAME.key));
                        center.setField(Fitness_Center.Field.DESCRIPTION, received.get(Protocol.Field.DESCRIPTION.key));
                        center.setField(Fitness_Center.Field.ADDRESS, received.get(Protocol.Field.ADDRESS.key));
                        if (received.get(Protocol.Field.ADDRESS_SECOND.key) != null) {
                            center.setField(Fitness_Center.Field.ADDRESS_SECOND, received.get(Protocol.Field.ADDRESS_SECOND.key));
                        }
                        center.setField(Fitness_Center.Field.ZIP_CODE, received.get(Protocol.Field.ZIP_CODE.key));
                        center.setField(Fitness_Center.Field.CITY, received.get(Protocol.Field.CITY.key));
                        if (received.get(Protocol.Field.CENTER_PHONE.key) != null) {
                            center.setField(Fitness_Center.Field.PHONE, received.get(Protocol.Field.CENTER_PHONE.key));
                        }

                        Database.update_entity(Database.Collections.Fitness_Center_Managers, manager);

                        Database.update_entity(Database.Collections.Fitness_Centers, center);
                        manager.setField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));
                        Database.update_entity(Database.Collections.Fitness_Center_Managers, manager);

                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_SUCCESS.code);
                    } else {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_ALREADY_EXISTS.code);
                    }

                } else {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_EMAIL_TAKEN.code);
                }
            } catch (Exception e) {
                if (manager != null) {
                    Database.delete_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.ID, manager.getField(Fitness_Center_Manager.Field.ID));
                }
                if (center != null) {
                    Database.delete_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, center.getField(Fitness_Center.Field.ID));
                }

                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
