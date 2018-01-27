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
import protocol.ProtocolIntranet;
import protocol.mobile.ResponseObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class CenterRegistration {
    public CenterRegistration(Router router) {
        router.route(HttpMethod.POST, ProtocolIntranet.Path.CENTER_REGISTER.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center center;
            Fitness_Center_Manager manager;
            Database database = Database.getInstance();

            try {
                manager = (Fitness_Center_Manager) database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(ProtocolIntranet.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(ProtocolIntranet.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    if (received.get(ProtocolIntranet.Field.NAME.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.CTR_ERROR_ERROR_NAME.code);
                    } else if (received.get(ProtocolIntranet.Field.DESCRIPTION.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.CTR_ERROR_ERROR_DESCRIPTION.code);
                    } else if (received.get(ProtocolIntranet.Field.ADDRESS.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.CTR_ERROR_ERROR_ADDRESS.code);
                    } else if (received.get(ProtocolIntranet.Field.ZIP_CODE.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.CTR_ERROR_ERROR_ZIP_CODE.code);
                    } else if (received.get(ProtocolIntranet.Field.CITY.key) == null) {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.CTR_ERROR_ERROR_CITY.code);
                    } else if (database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.NAME, received.get(ProtocolIntranet.Field.NAME.key)) == null) {
                        center = (Fitness_Center) database.new_entity(Database.Collections.Fitness_Centers);
                        center.setField(Fitness_Center.Field.NAME, received.get(ProtocolIntranet.Field.NAME.key));
                        center.setField(Fitness_Center.Field.DESCRIPTION, received.get(ProtocolIntranet.Field.DESCRIPTION.key));
                        center.setField(Fitness_Center.Field.ADDRESS, received.get(ProtocolIntranet.Field.ADDRESS.key));
                        center.setField(Fitness_Center.Field.ALBUM, new ArrayList<Fitness_Center.Picture_Describe>());
                        center.setField(Fitness_Center.Field.PUBLICATIONS, new ArrayList<Fitness_Center.Publication>());
                        if (received.get(ProtocolIntranet.Field.ADDRESS_SECOND.key) != null) {
                            center.setField(Fitness_Center.Field.ADDRESS_SECOND, received.get(ProtocolIntranet.Field.ADDRESS_SECOND.key));
                        }
                        center.setField(Fitness_Center.Field.ZIP_CODE, received.get(ProtocolIntranet.Field.ZIP_CODE.key));
                        center.setField(Fitness_Center.Field.CITY, received.get(ProtocolIntranet.Field.CITY.key));
                        if (received.get(ProtocolIntranet.Field.PHONE.key) != null) {
                            center.setField(Fitness_Center.Field.PHONE, received.get(ProtocolIntranet.Field.PHONE.key));
                        }
                        database.update_entity(Database.Collections.Fitness_Centers, center);
                        manager.setField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));
                        database.update_entity(Database.Collections.Fitness_Center_Managers, manager);
                        sending = new ResponseObject(false);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.CTR_REG_SUCCESS.code);
                    } else {
                        sending = new ResponseObject(true);
                        sending.put(ProtocolIntranet.Field.STATUS.key, ProtocolIntranet.Status.CTR_ERROR_ALREADY_EXISTS.code);
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
