package server.api.routes.intranet;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Feedback;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import org.bson.types.ObjectId;
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class GetFeedbacks {
    public GetFeedbacks(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.GET_FEEDBACKS.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Fitness_Center center;

            try {
                manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else if (!((Boolean)manager.getField(Fitness_Center_Manager.Field.IS_ACTIVE))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_ACCOUNT_INACTIVE.code);

                } else {
                    center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_NO_CENTER.code);
                    } else {
                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                        LinkedList<Database.Entity> feedbacks;

                        if ((Boolean)manager.getField(Fitness_Center_Manager.Field.IS_PRINCIPAL)) {
                            //TODO AJOUTER LE NOM DES MANAGERS
                            feedbacks = Database.find_entities(Database.Collections.Feedbacks,
                                    Feedback.Field.FITNESS_CENTER_ID,
                                    center.getField(Fitness_Center.Field.ID));

                        } else {
                            feedbacks = Database.find_entities(Database.Collections.Feedbacks,
                                    Feedback.Field.FITNESS_MANAGER_ID,
                                    manager.getField(Fitness_Center_Manager.Field.ID));

                        }
                        sending.put(Protocol.Field.FEEDBACKS.key, feedbacks);
                    }
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdSerializer()).create().toJson(sending));
        });
    }
}
