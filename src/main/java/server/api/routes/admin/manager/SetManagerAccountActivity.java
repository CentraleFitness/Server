package server.api.routes.admin.manager;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.OutlookInterface;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Administrator;
import model.entities.Fitness_Center_Manager;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.admin.Protocol;

import java.util.Map;
import java.util.Objects;

public class SetManagerAccountActivity {
    public SetManagerAccountActivity(Router router) {
        router.route(HttpMethod.PUT, Protocol.Path.MANAGER_ACTIVITY.path).handler(routingContext -> {

            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Administrator admin;

            try {
                admin = (Administrator) Database.find_entity(Database.Collections.Administrators, Administrator.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(admin.getField(Administrator.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);

                } else if (received.get(Protocol.Field.FITNESS_CENTER_MANAGER_ID.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else if (received.get(Protocol.Field.IS_ACTIVE.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                    Fitness_Center_Manager manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.ID, new ObjectId((String)received.get(Protocol.Field.FITNESS_CENTER_MANAGER_ID.key)));

                    Long time = System.currentTimeMillis();

                    manager.setField(Fitness_Center_Manager.Field.IS_ACTIVE, received.get(Protocol.Field.IS_ACTIVE.key));
                    manager.setField(Fitness_Center_Manager.Field.LAST_UPDATE_ACTIVITY, time);
                    manager.setField(Fitness_Center_Manager.Field.LAST_UPDATE_ADMIN_ID, admin.getField(Administrator.Field.ID));

                    manager.setField(Fitness_Center_Manager.Field.LAST_UPDATE_ADMIN_IS_MANAGER, false);

                    Database.update_entity(Database.Collections.Fitness_Center_Managers, manager);

                    String managerName = manager.getField(Fitness_Center_Manager.Field.FIRSTNAME) + " " +
                            manager.getField(Fitness_Center_Manager.Field.LASTNAME);
                    String adminName = admin.getField(Fitness_Center_Manager.Field.FIRSTNAME) + " " +
                            admin.getField(Fitness_Center_Manager.Field.LASTNAME);
                    String mailObject = "";
                    String mailContent = "";

                    if ((Boolean) received.get(Protocol.Field.IS_ACTIVE.key)) {

                        mailObject = "Votre compte est de nouveau actif";
                        mailContent = "Bonjour " + managerName + ",<br/><br/>" +
                                "Votre compte a été rendu actif par " + adminName + ", administrateur Centrale Fitness !<br/><br/>" +
                                "Vous pouvez de nouveau accéder à votre espace Centrale Fitness et administrer votre salle.<br/><br/>" +
                                "A bientôt,<br/><br/>" +
                                "L'équipe Centrale Fitness";

                    } else {

                        mailObject = "Votre compte a été rendu inactif";
                        mailContent = "Bonjour " + managerName + ",<br/><br/>" +
                                "Votre compte a été rendu inactif par " + adminName + ", administrateur Centrale Fitness.<br/><br/>" +
                                "Votre compte demeurera inactif à moins qu'un administrateur décide de revenir sur cette décision.<br/><br/>" +
                                "A bientôt peut être,<br/><br/>" +
                                "L'équipe Centrale Fitness";
                    }

                    OutlookInterface.outlookInterface.sendMail(
                            (String)manager.getField(Fitness_Center_Manager.Field.EMAIL),
                            mailObject,
                            mailContent
                    );

                    sending.put(Protocol.Field.ADMINISTRATOR_ID.key, admin.getField(Administrator.Field.ID));
                    sending.put(Protocol.Field.ADMINISTRATOR_NAME.key, admin.getField(Administrator.Field.FIRSTNAME) + " " + admin.getField(Administrator.Field.LASTNAME));
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
