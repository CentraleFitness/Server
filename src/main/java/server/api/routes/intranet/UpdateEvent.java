package server.api.routes.intranet;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.*;
import org.bson.types.ObjectId;
import protocol.ProtocolIntranet;
import protocol.intranet.ResponseObject;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class UpdateEvent {
    public UpdateEvent(Router router) {
        router.route(HttpMethod.POST, ProtocolIntranet.Path.UPDATE_EVENT.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Event event;
            Fitness_Center center;
            String pic64;
            Database database = Database.getInstance();

            try {
                manager = (Fitness_Center_Manager) database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(ProtocolIntranet.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(ProtocolIntranet.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.AUTH_ERROR_TOKEN.code);
                } else if (received.get(ProtocolIntranet.Field.EVENT_ID.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_MISSING_PARAM.code);
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
                        event = (Event) database.find_entity(Database.Collections.Events, Event.Field.ID, new ObjectId(received.get(ProtocolIntranet.Field.EVENT_ID.key).toString()));
                        Picture pic;

                        if ((pic64 = (String) received.get(ProtocolIntranet.Field.PICTURE.key)) != null) {
                            pic = (Picture) database.new_entity(Database.Collections.Pictures);
                            pic.setField(Picture.Field.PICTURE, pic64);
                            event.setField(Event.Field.PICTURE_ID, pic.getField(Picture.Field.ID));
                            event.setField(Event.Field.PICTURE, pic.getField(Picture.Field.PICTURE));
                            database.update_entity(Database.Collections.Pictures, pic);
                        } else {
                            event.setField(Event.Field.PICTURE_ID, null);
                            event.setField(Event.Field.PICTURE, "");
                        }

                        Long time = System.currentTimeMillis();

                        event.setField(Event.Field.TITLE, received.get(ProtocolIntranet.Field.TITLE.key));
                        event.setField(Event.Field.DESCRIPTION, received.get(ProtocolIntranet.Field.DESCRIPTION.key));
                        event.setField(Event.Field.START_DATE, received.get(ProtocolIntranet.Field.START_DATE.key));
                        event.setField(Event.Field.END_DATE, received.get(ProtocolIntranet.Field.END_DATE.key));
                        event.setField(Event.Field.UPDATE_DATE, time);
                        database.update_entity(Database.Collections.Events, event);

                        LinkedList<Database.Entity> event_users = database.find_entities(Database.Collections.TUPLE_Event_Users, TUPLE_Event_User.Field.EVENT_ID, event.getField(Event.Field.ID));
                        sending.put(ProtocolIntranet.Field.NB_SUBSCRIBERS.key, event_users.size());
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
