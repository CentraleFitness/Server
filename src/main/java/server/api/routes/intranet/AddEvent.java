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
import model.entities.Picture;
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.Map;
import java.util.Objects;

public class AddEvent {
    public AddEvent(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.ADD_EVENT.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Event event;
            Fitness_Center center;
            String pic64;

            try {
                manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else if (!((Boolean)manager.getField(Fitness_Center_Manager.Field.IS_ACTIVE))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_ACCOUNT_INACTIVE.code);

                } else if (received.get(Protocol.Field.TITLE.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);
                } else if (received.get(Protocol.Field.DESCRIPTION.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);
                } else if (received.get(Protocol.Field.START_DATE.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);
                } else if (received.get(Protocol.Field.END_DATE.key) == null) {
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
                        event = (Event) Database.new_entity(Database.Collections.Events);
                        Picture pic;

                        if ((pic64 = (String) received.get(Protocol.Field.PICTURE.key)) != null) {
                            pic = (Picture) Database.new_entity(Database.Collections.Pictures);
                            pic.setField(Picture.Field.PICTURE, pic64);
                            event.setField(Event.Field.PICTURE_ID, pic.getField(Picture.Field.ID));
                            event.setField(Event.Field.PICTURE, pic.getField(Picture.Field.PICTURE));
                            Database.update_entity(Database.Collections.Pictures, pic);
                        } else {
                            event.setField(Event.Field.PICTURE_ID, null);
                            event.setField(Event.Field.PICTURE, "");
                        }

                        Long time = System.currentTimeMillis();

                        event.setField(Event.Field.TITLE, received.get(Protocol.Field.TITLE.key));
                        event.setField(Event.Field.DESCRIPTION, received.get(Protocol.Field.DESCRIPTION.key));
                        event.setField(Event.Field.START_DATE, received.get(Protocol.Field.START_DATE.key));
                        event.setField(Event.Field.END_DATE, received.get(Protocol.Field.END_DATE.key));
                        event.setField(Event.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));
                        event.setField(Event.Field.UPDATE_DATE, time);
                        event.setField(Event.Field.CREATION_DATE, time);
                        event.setField(Event.Field.IS_DELETED, false);
                        event.setField(Event.Field.DELETION_CAUSE, "");
                        Database.update_entity(Database.Collections.Events, event);
                        sending.put(Protocol.Field.EVENT_ID.key, event.getField(Event.Field.ID).toString());
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
