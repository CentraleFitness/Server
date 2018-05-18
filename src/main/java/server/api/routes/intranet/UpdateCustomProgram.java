package server.api.routes.intranet;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.CustomProgram;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import model.entities.Picture;
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UpdateCustomProgram {
    public UpdateCustomProgram(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.UPDATE_CUSTOM_PROGRAM.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            CustomProgram custom_program;
            Fitness_Center center;
            String pic64;

            try {
                manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else if (received.get(Protocol.Field.CUSTOM_PROGRAM_ID.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);
                }  else if (received.get(Protocol.Field.NAME.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);
                } else if (received.get(Protocol.Field.NB_ACTIVITIES.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);
                } else if (received.get(Protocol.Field.TOTAL_TIME.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);
                } else if (received.get(Protocol.Field.AVAILABLE.key) == null) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_MISSING_PARAM.code);
                } else if (received.get(Protocol.Field.ACTIVITIES.key) == null) {
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

                        custom_program = (CustomProgram) Database.find_entity(Database.Collections.CustomPrograms, CustomProgram.Field.ID, new ObjectId(received.get(Protocol.Field.CUSTOM_PROGRAM_ID.key).toString()));
                        Picture pic;

                        if ((pic64 = (String) received.get(Protocol.Field.PICTURE.key)) != null) {
                            pic = (Picture) Database.new_entity(Database.Collections.Pictures);
                            pic.setField(Picture.Field.PICTURE, pic64);
                            custom_program.setField(CustomProgram.Field.PICTURE_ID, pic.getField(Picture.Field.ID));
                            custom_program.setField(CustomProgram.Field.PICTURE, pic.getField(Picture.Field.PICTURE));
                            Database.update_entity(Database.Collections.Pictures, pic);
                        }

                        Long time = System.currentTimeMillis();

                        custom_program.setField(CustomProgram.Field.NAME, received.get(Protocol.Field.NAME.key));
                        custom_program.setField(CustomProgram.Field.NB_ACTIVITIES, received.get(Protocol.Field.NB_ACTIVITIES.key));
                        custom_program.setField(CustomProgram.Field.TOTAL_TIME, received.get(Protocol.Field.TOTAL_TIME.key));
                        custom_program.setField(CustomProgram.Field.AVAILABLE, received.get(Protocol.Field.AVAILABLE.key));
                        custom_program.setField(CustomProgram.Field.UPDATE_DATE, time);

                        @SuppressWarnings("unchecked")
                        ArrayList<HashMap<String, Object>> activities_array = (ArrayList<HashMap<String, Object>>) received.get(Protocol.Field.ACTIVITIES.key);

                        ArrayList<Document> activities = new ArrayList<>();
                        Document doc;

                        for (HashMap<String, Object> cur : activities_array) {
                            doc = new Document();
                            doc.put("_id", new ObjectId(cur.get("_id").toString()));
                            doc.put("name", cur.get("name").toString());
                            doc.put("time", cur.get("time"));
                            doc.put("icon", cur.get("icon").toString());
                            activities.add(doc);
                        }

                        custom_program.setField(CustomProgram.Field.ACTIVITIES, activities);

                        Database.update_entity(Database.Collections.CustomPrograms, custom_program);
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
