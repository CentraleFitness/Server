package server.api.routes.intranet.manager;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.OutlookInterface;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.util.Map;
import java.util.Objects;

public class SetManagerAccountActivity {
    public SetManagerAccountActivity(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.MANAGER_ACTIVITY.path).handler(routingContext -> {

            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager me;
            Fitness_Center center;
            Boolean sendMail = false;
            String mailContent = "";
            String mailObject = "";
            Fitness_Center_Manager manager = null;

            try {
                me = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(me.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);

                } else if (received.get(Protocol.Field.FITNESS_CENTER_MANAGER_ID.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else if (received.get(Protocol.Field.IS_ACTIVE.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else {

                    center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, me.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_NO_CENTER.code);
                    } else if (!(Boolean)me.getField(Fitness_Center_Manager.Field.IS_PRINCIPAL)) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_NOT_PRINCIPAL.code);

                    } else {
                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                        manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.ID, new ObjectId((String) received.get(Protocol.Field.FITNESS_CENTER_MANAGER_ID.key)));

                        Long time = System.currentTimeMillis();

                        manager.setField(Fitness_Center_Manager.Field.IS_ACTIVE, received.get(Protocol.Field.IS_ACTIVE.key));
                        manager.setField(Fitness_Center_Manager.Field.LAST_UPDATE_ACTIVITY, time);
                        manager.setField(Fitness_Center_Manager.Field.LAST_UPDATE_ADMIN_ID, me.getField(Fitness_Center_Manager.Field.ID));

                        manager.setField(Fitness_Center_Manager.Field.LAST_UPDATE_ADMIN_IS_MANAGER, true);

                        Database.update_entity(Database.Collections.Fitness_Center_Managers, manager);

                        String managerName = manager.getField(Fitness_Center_Manager.Field.FIRSTNAME) + " " +
                                manager.getField(Fitness_Center_Manager.Field.LASTNAME);
                        String adminName = me.getField(Fitness_Center_Manager.Field.FIRSTNAME) + " " +
                                me.getField(Fitness_Center_Manager.Field.LASTNAME);
                        String clubName = (String) center.getField(Fitness_Center.Field.NAME);
                        mailObject = "";
                        mailContent = "";

                        if ((Boolean) received.get(Protocol.Field.IS_ACTIVE.key)) {

                            mailObject = "Votre compte est de nouveau actif";
                            mailContent = "Bonjour " + managerName + ",<br/><br/>" +
                                    "Votre compte a &eacute;t&eacute; rendu actif par " + adminName + ", g&eacute;rant principal de la salle " + clubName + " !<br/><br/>" +
                                    "Vous pouvez de nouveau acc&eacute;der &agrave; votre espace Centrale Fitness et administrer votre salle.<br/><br/>" +
                                    "A bient&ocirc;t,<br/><br/>" +
                                    "L'&eacute;quipe Centrale Fitness";

                        } else {

                            mailObject = "Votre compte a été rendu inactif";
                            mailContent = "Bonjour " + managerName + ",<br/><br/>" +
                                    "Votre compte a &eacute;t&eacute; rendu inactif par " + adminName + ", g&eacute;rant principal de la salle " + clubName + ".<br/><br/>" +
                                    "Votre compte demeurera inactif &agrave; moins qu'un administrateur d&eacute;cide de revenir sur cette d&eacute;cision.<br/><br/>" +
                                    "A bient&ocirc;t peut &ecirc;tre,<br/><br/>" +
                                    "L'&eacute;quipe Centrale Fitness";
                        }

                        sendMail = true;

                        sending.put(Protocol.Field.ADMINISTRATOR_ID.key, me.getField(Fitness_Center_Manager.Field.ID));
                        sending.put(Protocol.Field.ADMINISTRATOR_NAME.key, me.getField(Fitness_Center_Manager.Field.FIRSTNAME) + " " + me.getField(Fitness_Center_Manager.Field.LASTNAME));
                    }
                }

            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdSerializer()).create().toJson(sending));
            if (sendMail) {
                OutlookInterface.outlookInterface.sendMail(
                        (String)manager.getField(Fitness_Center_Manager.Field.EMAIL),
                        mailObject,
                        mailContent
                );
            }
        });
    }
}
