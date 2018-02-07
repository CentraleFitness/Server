package server.api.routes.intranet;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import model.entities.Picture;
import protocol.ProtocolIntranet;
import protocol.ResponseObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class CenterAddPictureToAlbum {
    public CenterAddPictureToAlbum(Router router) {
        router.route(HttpMethod.POST, ProtocolIntranet.Path.CENTER_INCREASE_ALBUM.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;
            Fitness_Center center;
            String pic64;
            Database database = Database.getInstance();

            try {
                manager = (Fitness_Center_Manager) database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(ProtocolIntranet.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(ProtocolIntranet.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.AUTH_ERROR_TOKEN.code);
                } else if ((pic64 = (String) received.get(ProtocolIntranet.Field.PICTURE.key)) == null) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_MISSING_PARAM.code);
                }
                else {
                    center = (Fitness_Center) database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.MGR_ERROR_NO_CENTER.code);
                    } else {
                        sending = new ResponseObject(false);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.GENERIC_OK.code);
                        Picture pic = (Picture) database.new_entity(Database.Collections.Pictures);
                        pic.setField(Picture.Field.PICTURE, pic64);

                        @SuppressWarnings("unchecked")
                        ArrayList<Fitness_Center.Picture_Describe> album = (ArrayList<Fitness_Center.Picture_Describe>) center.getField(Fitness_Center.Field.ALBUM);
                        Fitness_Center.Picture_Describe picture_describe = new Fitness_Center.Picture_Describe();
                        picture_describe.setField(Fitness_Center.Picture_Describe.Field.PICTURE_ID, pic.getField(Picture.Field.ID));
                        picture_describe.setField(Fitness_Center.Picture_Describe.Field.PICTURE, pic.getField(Picture.Field.PICTURE));
                        picture_describe.setField(Fitness_Center.Picture_Describe.Field.TITLE, received.get(ProtocolIntranet.Field.TITLE.key));
                        picture_describe.setField(Fitness_Center.Picture_Describe.Field.DESCRIPTION, received.get(ProtocolIntranet.Field.DESCRIPTION.key));
                        picture_describe.setField(Fitness_Center.Picture_Describe.Field.CREATION_DATE, System.currentTimeMillis());
                        album.add(picture_describe);
                        //center.setField(Fitness_Center.Field.ALBUM, album);

                        database.update_entity(Database.Collections.Pictures, pic);
                        database.update_entity(Database.Collections.Fitness_Centers, center);
                        sending.put(ProtocolIntranet.Field.PICTURE_ID.key, pic.getField(Picture.Field.ID).toString());
                    }
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

    }
}
