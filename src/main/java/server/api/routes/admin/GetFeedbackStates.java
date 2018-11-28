package server.api.routes.admin;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.client.FindIterable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Administrator;
import model.entities.Feedback_State;
import model.entities.Fitness_Center_Manager;
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.admin.Protocol;

import java.util.*;

public class GetFeedbackStates {
    public GetFeedbackStates(Router router) {
        router.route(HttpMethod.GET, Protocol.Path.FEEDBACK_STATES.path).handler(routingContext -> {

            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Administrator admin;

            try {
                admin = (Administrator) Database.find_entity(Database.Collections.Administrators, Administrator.Field.EMAIL, Token.decodeToken(routingContext.request().getParam(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(admin.getField(Administrator.Field.TOKEN), routingContext.request().getParam(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    sending = new ResponseObject(false);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                    @SuppressWarnings("unchecked")
                    FindIterable<Feedback_State> findIterable = (FindIterable<Feedback_State>) Database.collections.get(Database.Collections.Feedback_States).find();
                    List<Map<String,Object>> feedback_states = new ArrayList<>();
                    HashMap<String,Object> cur;
                    for (Document doc : findIterable) {
                        cur = new HashMap<>();
                        cur.put("_id", doc.getObjectId("_id").toString());
                        cur.put("code", doc.getInteger("code"));
                        cur.put("text_fr", doc.getString("text_fr"));
                        cur.put("text_en", doc.getString("text_en"));
                        feedback_states.add(cur);
                    }
                    sending.put(Protocol.Field.FEEDBACK_STATES.key, feedback_states);
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
