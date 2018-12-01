package server.api.routes.intranet.feedback;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Feedback;
import model.entities.Feedback_State;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class AddResponseFeedback {
    public AddResponseFeedback(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.ADD_RESPONSE_FEEDBACK.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Fitness_Center center;
            Feedback feedback;

            try {
                manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else if (!((Boolean)manager.getField(Fitness_Center_Manager.Field.IS_ACTIVE))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_ACCOUNT_INACTIVE.code);

                } else if (received.get(Protocol.Field.CONTENT.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);
                } else if (received.get(Protocol.Field.FEEDBACK_ID.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);
                } else {
                    center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_NO_CENTER.code);
                    } else {
                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                        Feedback_State state = (Feedback_State) Database.find_entity(Database.Collections.Feedback_States, Feedback_State.Field.CODE, 1);

                        Long time = System.currentTimeMillis();

                        feedback = (Feedback) Database.find_entity(Database.Collections.Feedbacks, Feedback.Field.ID, new ObjectId((String)received.get(Protocol.Field.FEEDBACK_ID.key)));

                        feedback.setField(Feedback.Field.FEEDBACK_STATE_ID, state.getField(Feedback_State.Field.ID));
                        feedback.setField(Feedback.Field.FEEDBACK_STATE, state.getField(Feedback_State.Field.CODE));
                        feedback.setField(Feedback.Field.UPDATE_DATE, time);

                        Document doc = new Document();
                        doc.put("content", received.get(Protocol.Field.CONTENT.key));
                        doc.put("author_id", manager.getField(Fitness_Center_Manager.Field.ID));
                        doc.put("author", manager.getField(Fitness_Center_Manager.Field.FIRSTNAME) + " " +
                                manager.getField(Fitness_Center_Manager.Field.LASTNAME));
                        doc.put("is_admin", false);
                        doc.put("date", time);

                        ArrayList<Document> responses = (ArrayList<Document>) feedback.getField(Feedback.Field.RESPONSES);

                        responses.add(doc);

                        feedback.setField(Feedback.Field.RESPONSES, responses);

                        Database.update_entity(Database.Collections.Feedbacks, feedback);

                        sending.put(Protocol.Field.FITNESS_MANAGER_NAME.key,
                                manager.getField(Fitness_Center_Manager.Field.FIRSTNAME) + " " +
                                        manager.getField(Fitness_Center_Manager.Field.LASTNAME));
                    }
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
