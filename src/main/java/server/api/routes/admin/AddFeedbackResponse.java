package server.api.routes.admin;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Administrator;
import model.entities.Feedback;
import model.entities.Feedback_State;
import model.entities.Fitness_Center_Manager;
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.admin.Protocol;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class AddFeedbackResponse {
    public AddFeedbackResponse(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.FEEDBACK_RESPONSE.path).handler(routingContext -> {

            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Administrator admin;

            try {
                admin = (Administrator) Database.find_entity(Database.Collections.Administrators, Administrator.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(admin.getField(Administrator.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);

                } else if (received.get(Protocol.Field.CONTENT.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else if (received.get(Protocol.Field.FEEDBACK_ID.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else if (received.get(Protocol.Field.FEEDBACK_STATE_CODE.key) == null) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);

                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                    Feedback_State state = (Feedback_State) Database.find_entity(Database.Collections.Feedback_States, Feedback_State.Field.CODE, received.get(Protocol.Field.FEEDBACK_STATE_CODE.key));

                    Long time = System.currentTimeMillis();

                    Feedback feedback = (Feedback) Database.find_entity(Database.Collections.Feedbacks, Feedback.Field.ID, new ObjectId((String)received.get(protocol.intranet.Protocol.Field.FEEDBACK_ID.key)));

                    feedback.setField(Feedback.Field.FEEDBACK_STATE_ID, state.getField(Feedback_State.Field.ID));
                    feedback.setField(Feedback.Field.FEEDBACK_STATE, state.getField(Feedback_State.Field.CODE));
                    feedback.setField(Feedback.Field.UPDATE_DATE, time);

                    Document doc = new Document();
                    doc.put("content", received.get(protocol.intranet.Protocol.Field.CONTENT.key));
                    doc.put("author_id", admin.getField(Administrator.Field.ID));
                    doc.put("author", admin.getField(Administrator.Field.FIRSTNAME) + " " + admin.getField(Administrator.Field.LASTNAME));
                    doc.put("is_admin", true);
                    doc.put("date", time);

                    ArrayList<Document> responses = (ArrayList<Document>) feedback.getField(Feedback.Field.RESPONSES);

                    responses.add(doc);

                    feedback.setField(Feedback.Field.RESPONSES, responses);

                    Database.update_entity(Database.Collections.Feedbacks, feedback);

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
