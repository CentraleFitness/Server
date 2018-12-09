package server.api.routes.intranet.account;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.Map;
import java.util.Objects;

public class CenterUpdateProfile {
    public CenterUpdateProfile(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.CENTER_UPDATE_PROFILE.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;

            try {
                manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else if (!((Boolean)manager.getField(Fitness_Center_Manager.Field.IS_ACTIVE))) {

                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_ACCOUNT_INACTIVE.code);

                } else {
                    Fitness_Center center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_NO_CENTER.code);
                    } else {
                        if (received.get(Protocol.Field.NAME.key) == null) {
                            sending = new ResponseObject(true);
                            sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_ERROR_NAME.code);
                        } else if (received.get(Protocol.Field.DESCRIPTION.key) == null) {
                            sending = new ResponseObject(true);
                            sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_ERROR_DESCRIPTION.code);
                        } else if (received.get(Protocol.Field.ADDRESS.key) == null) {
                            sending = new ResponseObject(true);
                            sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_ERROR_ADDRESS.code);
                        } else if (received.get(Protocol.Field.ZIP_CODE.key) == null) {
                            sending = new ResponseObject(true);
                            sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_ERROR_ZIP_CODE.code);
                        } else if (received.get(Protocol.Field.CITY.key) == null) {
                            sending = new ResponseObject(true);
                            sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_ERROR_CITY.code);
                        } else {
                            sending = new ResponseObject(false);
                            sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);
                            center.setField(Fitness_Center.Field.NAME, received.get(Protocol.Field.NAME.key));
                            center.setField(Fitness_Center.Field.DESCRIPTION, received.get(Protocol.Field.DESCRIPTION.key));
                            center.setField(Fitness_Center.Field.ADDRESS, received.get(Protocol.Field.ADDRESS.key));
                            if (received.get(Protocol.Field.ADDRESS_SECOND.key) != null) {
                                center.setField(Fitness_Center.Field.ADDRESS_SECOND, received.get(Protocol.Field.ADDRESS_SECOND.key));
                            } else {
                                center.setField(Fitness_Center.Field.ADDRESS_SECOND, "");
                            }
                            center.setField(Fitness_Center.Field.ZIP_CODE, received.get(Protocol.Field.ZIP_CODE.key));
                            center.setField(Fitness_Center.Field.CITY, received.get(Protocol.Field.CITY.key));
                            if (received.get(Protocol.Field.PHONE.key) != null) {
                                center.setField(Fitness_Center.Field.PHONE, received.get(Protocol.Field.PHONE.key));
                            } else {
                                center.setField(Fitness_Center.Field.PHONE, "");
                            }
                            Database.update_entity(Database.Collections.Fitness_Centers, center);
                        }
                    }
                }
            } catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });
    }
}
