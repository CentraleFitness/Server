package server.api.routes.intranet.account;

import Tools.LogManager;
import Tools.OutlookInterface;
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
                    } else if (received.get(Protocol.Field.SIRET.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_ERROR_SIRET.code);
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
                    } else if (Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.SIRET, received.get(Protocol.Field.SIRET.key)) == null) {

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

                        manager.setField(Fitness_Center_Manager.Field.IS_PRINCIPAL, true);

                        center = (Fitness_Center) Database.new_entity(Database.Collections.Fitness_Centers);
                        center.setField(Fitness_Center.Field.NAME, received.get(Protocol.Field.NAME.key));
                        center.setField(Fitness_Center.Field.SIRET, received.get(Protocol.Field.SIRET.key));
                        center.setField(Fitness_Center.Field.DESCRIPTION, received.get(Protocol.Field.DESCRIPTION.key));
                        center.setField(Fitness_Center.Field.ADDRESS, received.get(Protocol.Field.ADDRESS.key));
                        if (received.get(Protocol.Field.ADDRESS_SECOND.key) != null) {
                            center.setField(Fitness_Center.Field.ADDRESS_SECOND, received.get(Protocol.Field.ADDRESS_SECOND.key));
                        } else {
                            center.setField(Fitness_Center.Field.ADDRESS_SECOND, "");
                        }
                        center.setField(Fitness_Center.Field.ZIP_CODE, received.get(Protocol.Field.ZIP_CODE.key));
                        center.setField(Fitness_Center.Field.CITY, ((String)received.get(Protocol.Field.CITY.key)).toUpperCase());
                        if (received.get(Protocol.Field.CENTER_PHONE.key) != null) {
                            center.setField(Fitness_Center.Field.PHONE, received.get(Protocol.Field.CENTER_PHONE.key));
                        } else {
                            center.setField(Fitness_Center.Field.PHONE, "");
                        }
                        center.setField(Fitness_Center.Field.CREATION_DATE, time);

                        Database.update_entity(Database.Collections.Fitness_Center_Managers, manager);

                        Database.update_entity(Database.Collections.Fitness_Centers, center);
                        manager.setField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));
                        Database.update_entity(Database.Collections.Fitness_Center_Managers, manager);

                        if (Database.collections.get(Database.Collections.Statistics).countDocuments(Filters.eq("fitness_center_id", center.getField(Fitness_Center.Field.ID))) == 0) {
                            Statistic statistic = (Statistic) Database.new_entity(Database.Collections.Statistics);
                            statistic.setField(Statistic.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));
                            statistic.setField(Statistic.Field.PRODUCTION_DAY, 0);
                            statistic.setField(Statistic.Field.PRODUCTION_MONTH, 0);
                            statistic.setField(Statistic.Field.FREQUENTATION_DAY, 0);
                            statistic.setField(Statistic.Field.FREQUENTATION_MONTH, 0);

                            Database.update_entity(Database.Collections.Statistics, statistic);
                        }

                        String managerName = manager.getField(Fitness_Center_Manager.Field.FIRSTNAME) + " " +
                                manager.getField(Fitness_Center_Manager.Field.LASTNAME);
                        String clubName = (String) center.getField(Fitness_Center.Field.NAME);

                        String mailContent = "Bonjour " + managerName + ",<br/><br/>" +
                                "Bienvenue chez Centrale Fitness !<br/>" +
                                "Votre compte de gérant principal et la salle " + clubName + " ont été créés avec succès !<br/><br/>" +
                                "Votre compte est maintenant en attente de validation par nos équipes.<br/>" +
                                "Vous recevrez un email lors de la validation de votre compte.<br/><br/>" +
                                "A bientôt,<br/><br/>" +
                                "L'équipe Centrale Fitness";

                        OutlookInterface.outlookInterface.sendMail(
                                (String)manager.getField(Fitness_Center_Manager.Field.EMAIL),
                                "Inscription à Centrale Fitness",
                                mailContent
                        );

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
