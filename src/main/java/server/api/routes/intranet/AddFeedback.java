package server.api.routes.intranet;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Feedback;
import model.entities.Feedback_State;
import model.entities.Fitness_Center_Manager;
import protocol.ProtocolIntranet;
import protocol.mobile.ResponseObject;

import java.util.Map;
import java.util.Objects;

public class AddFeedback {
    public AddFeedback(Router router) {
        router.route(HttpMethod.POST, ProtocolIntranet.Path.ADD_FEEDBACK.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Feedback feedback;
            Database database = Database.getInstance();

            try {
                manager = (Fitness_Center_Manager) database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(ProtocolIntranet.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(ProtocolIntranet.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.AUTH_ERROR_TOKEN.code);
                } else if (received.get(ProtocolIntranet.Field.TITLE.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_MISSING_PARAM.code);
                } else if (received.get(ProtocolIntranet.Field.DESCRIPTION.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_MISSING_PARAM.code);
                } else {
                    sending = new ResponseObject(false);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_OK.code);

                    Feedback_State state = (Feedback_State) database.find_entity(Database.Collections.Feedback_States, Feedback_State.Field.CODE, 1);
                    Long time = System.currentTimeMillis();

                    feedback = (Feedback) database.new_entity(Database.Collections.Feedbacks);
                    feedback.setField(Feedback.Field.TITLE, received.get(ProtocolIntranet.Field.TITLE.key));
                    feedback.setField(Feedback.Field.DESCRIPTION, received.get(ProtocolIntranet.Field.DESCRIPTION.key));
                    feedback.setField(Feedback.Field.FEEDBACK_STATE_ID, state.getField(Feedback_State.Field.ID));
                    feedback.setField(Feedback.Field.FITNESS_MANAGER_ID, manager.getField(Fitness_Center_Manager.Field.ID));
                    feedback.setField(Feedback.Field.CREATION_DATE, time);
                    feedback.setField(Feedback.Field.UPDATE_DATE, time);
                    database.update_entity(Database.Collections.Feedbacks, feedback);
                    sending.put(ProtocolIntranet.Field.FEEDBACK_ID.key, feedback.getField(Feedback.Field.ID).toString());
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
