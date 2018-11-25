package server.api.routes.intranet;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class UpdateEvent {
    public UpdateEvent(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.UPDATE_EVENT.path).handler(routingContext -> {
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

                } else if (received.get(Protocol.Field.EVENT_ID.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);
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
                        event = (Event) Database.find_entity(Database.Collections.Events, Event.Field.ID, new ObjectId(received.get(Protocol.Field.EVENT_ID.key).toString()));
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
                        event.setField(Event.Field.UPDATE_DATE, time);
                        Database.update_entity(Database.Collections.Events, event);

                        LinkedList<Database.Entity> event_users = Database.find_entities(Database.Collections.TUPLE_Event_Users, TUPLE_Event_User.Field.EVENT_ID, event.getField(Event.Field.ID));
                        sending.put(Protocol.Field.NB_SUBSCRIBERS.key, event_users.size());
                    }
                }
            } catch (Exception e) {
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
                System.out.println("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
