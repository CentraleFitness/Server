package server.api.routes.intranet.account;

import Tools.LogManager;
import Tools.PasswordAuthentication;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.client.model.Filters;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import model.entities.Statistic;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.util.Map;

public class RegisterManager {
    public RegisterManager(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.MANAGER_REGISTRATION.path).handler(routingContext -> {
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

                    if (received.get(Protocol.Field.SIRET.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_ERROR_SIRET.code);
                    } else if ((center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.SIRET, received.get(Protocol.Field.SIRET.key))) != null) {

                        Long time = System.currentTimeMillis();

                        manager = (Fitness_Center_Manager) Database.new_entity(Database.Collections.Fitness_Center_Managers);
                        manager.setField(Fitness_Center_Manager.Field.FIRSTNAME,  ((String)received.get(Protocol.Field.FIRSTNAME.key)).substring(0, 1).toUpperCase() + ((String)received.get(Protocol.Field.FIRSTNAME.key)).substring(1));
                        manager.setField(Fitness_Center_Manager.Field.LASTNAME, ((String)received.get(Protocol.Field.LASTNAME.key)).toUpperCase());
                        manager.setField(Fitness_Center_Manager.Field.PHONE, received.get(Protocol.Field.PHONE.key));
                        manager.setField(Fitness_Center_Manager.Field.EMAIL, received.get(Protocol.Field.EMAIL.key));
                        manager.setField(Fitness_Center_Manager.Field.PASSWORD_HASH, new PasswordAuthentication().hash(((String) received.get(Protocol.Field.PASSWORD.key)).toCharArray()));
                        manager.setField(Fitness_Center_Manager.Field.TOKEN, new Token((String) received.get(Protocol.Field.EMAIL.key), (String) received.get(Protocol.Field.PASSWORD.key)).generate());
                        manager.setField(Fitness_Center_Manager.Field.CREATION_DATE, time);

                        manager.setField(Fitness_Center_Manager.Field.IS_ACTIVE, false);
                        manager.setField(Fitness_Center_Manager.Field.LAST_UPDATE_ACTIVITY, 0L);
                        manager.setField(Fitness_Center_Manager.Field.LAST_UPDATE_ADMIN_ID, null);

                        manager.setField(Fitness_Center_Manager.Field.LAST_UPDATE_ADMIN_IS_MANAGER, false);

                        manager.setField(Fitness_Center_Manager.Field.IS_REFUSED, false);
                        manager.setField(Fitness_Center_Manager.Field.IS_VALIDATED, false);
                        manager.setField(Fitness_Center_Manager.Field.VALIDATION_DATE, 0L);
                        manager.setField(Fitness_Center_Manager.Field.VALIDATOR_ADMIN_ID, null);

                        manager.setField(Fitness_Center_Manager.Field.VALIDATOR_ADMIN_IS_MANAGER, false);

                        manager.setField(Fitness_Center_Manager.Field.IS_PRINCIPAL, false);

                        manager.setField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));

                        Database.update_entity(Database.Collections.Fitness_Center_Managers, manager);


                        /*if (Database.collections.get(Database.Collections.Statistics).count(Filters.eq("fitness_center_id", center.getField(Fitness_Center.Field.ID))) == 0) {
                            Statistic statistic = (Statistic) Database.new_entity(Database.Collections.Statistics);
                            statistic.setField(Statistic.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));
                            statistic.setField(Statistic.Field.PRODUCTION_DAY, 0);
                            statistic.setField(Statistic.Field.PRODUCTION_MONTH, 0);
                            statistic.setField(Statistic.Field.FREQUENTATION_DAY, 0);
                            statistic.setField(Statistic.Field.FREQUENTATION_MONTH, 0);

                            Database.update_entity(Database.Collections.Statistics, statistic);
                        }*/

                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_SUCCESS.code);
                    } else {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_DOESNT_EXIST.code);
                    }

                } else {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.REG_ERROR_EMAIL_TAKEN.code);
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
