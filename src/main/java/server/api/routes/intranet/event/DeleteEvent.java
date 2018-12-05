package server.api.routes.intranet.event;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.DisplayConfiguration;
import model.entities.Event;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import org.bson.types.ObjectId;
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class DeleteEvent {
    public DeleteEvent(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.DELETE_EVENT.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Event event;
            Fitness_Center center;

            try {
                manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else if (!((Boolean)manager.getField(Fitness_Center_Manager.Field.IS_ACTIVE))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_ACCOUNT_INACTIVE.code);

                } else if (received.get(Protocol.Field.EVENT_ID.key) == null) {
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
                        event = (Event) Database.find_entity(Database.Collections.Events, Event.Field.ID, new ObjectId(received.get(Protocol.Field.EVENT_ID.key).toString()));

                        Long time = System.currentTimeMillis();

                        event.setField(Event.Field.IS_DELETED, true);
                        if (received.get(Protocol.Field.DELETION_CAUSE.key) == null) {
                            event.setField(Event.Field.DELETION_CAUSE, "");
                        } else {
                            event.setField(Event.Field.DELETION_CAUSE, received.get(Protocol.Field.DELETION_CAUSE.key));
                        }
                        event.setField(Event.Field.UPDATE_DATE, time);
                        Database.update_entity(Database.Collections.Events, event);

                        DisplayConfiguration display = (DisplayConfiguration) Database.find_entity(Database.Collections.DisplayConfigurations, DisplayConfiguration.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));

                        ArrayList<ObjectId> selected_events = (ArrayList<ObjectId>) display.getField(DisplayConfiguration.Field.SELECTED_EVENTS);
                        Predicate<ObjectId> pred = evt -> evt.toString().equals(event.getField(Event.Field.ID).toString());
                        selected_events.removeIf(pred);
                        display.setField(DisplayConfiguration.Field.SELECTED_EVENTS, selected_events);
                        Database.update_entity(Database.Collections.DisplayConfigurations, display);
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
