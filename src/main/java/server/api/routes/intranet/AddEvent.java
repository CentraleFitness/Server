package server.api.routes.intranet;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Event;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import protocol.ProtocolIntranet;
import protocol.ResponseObject;

import java.util.Map;
import java.util.Objects;

public class AddEvent {
    public AddEvent(Router router) {
        router.route(HttpMethod.POST, ProtocolIntranet.Path.ADD_EVENT.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Event event;
            Fitness_Center center;
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
                } else if (received.get(ProtocolIntranet.Field.START_DATE.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_MISSING_PARAM.code);
                } else if (received.get(ProtocolIntranet.Field.END_DATE.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_MISSING_PARAM.code);
                } else {

                    center = (Fitness_Center) database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.MGR_ERROR_NO_CENTER.code);
                    } else {
                        sending = new ResponseObject(false);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_OK.code);

                        Long time = System.currentTimeMillis();

                        event = (Event) database.new_entity(Database.Collections.Events);
                        event.setField(Event.Field.TITLE, received.get(ProtocolIntranet.Field.TITLE.key));
                        event.setField(Event.Field.DESCRIPTION, received.get(ProtocolIntranet.Field.DESCRIPTION.key));
                        event.setField(Event.Field.START_DATE, received.get(ProtocolIntranet.Field.START_DATE.key));
                        event.setField(Event.Field.END_DATE, received.get(ProtocolIntranet.Field.END_DATE.key));
                        event.setField(Event.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));
                        event.setField(Event.Field.UPDATE_DATE, time);
                        database.update_entity(Database.Collections.Events, event);
                        sending.put(ProtocolIntranet.Field.EVENT_ID.key, event.getField(Event.Field.ID).toString());
                    }
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
