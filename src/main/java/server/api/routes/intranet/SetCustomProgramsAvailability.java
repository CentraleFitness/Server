package server.api.routes.intranet;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.client.model.Filters;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.CustomProgram;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import org.bson.Document;
import org.bson.conversions.Bson;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class SetCustomProgramsAvailability {
    public SetCustomProgramsAvailability(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.SET_CUSTOM_PROGRAMS_AVAILABILITY.path).handler(routingContext -> {
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
                } else if (received.get(Protocol.Field.ENABLED.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);
                } else if (received.get(Protocol.Field.DISABLED.key) == null) {
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


                        /*Bson filter = Filters.and(
                                Filters.eq(CustomProgram.Field.FITNESS_CENTER_ID.get_key(), center.getField(Fitness_Center.Field.ID)),
                                Filters.eq(Event.Field.IS_DELETED.get_key(), false)
                        );
                        @SuppressWarnings("unchecked")
                        ArrayList<Document> events = (ArrayList<Document>) Database.collections.get(Database.Collections.Events).updateMany();*/
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
