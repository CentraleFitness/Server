package server.api.routes.intranet;

import Tools.LogManager;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.DisplayConfiguration;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import protocol.intranet.Protocol;
import protocol.ResponseObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class CenterRegistration {
    public CenterRegistration(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.CENTER_REGISTER.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center center;
            Fitness_Center_Manager manager;

            try {
                manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
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
                    } else if (Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.NAME, received.get(Protocol.Field.NAME.key)) == null) {
                        center = (Fitness_Center) Database.new_entity(Database.Collections.Fitness_Centers);
                        center.setField(Fitness_Center.Field.NAME, received.get(Protocol.Field.NAME.key));
                        center.setField(Fitness_Center.Field.DESCRIPTION, received.get(Protocol.Field.DESCRIPTION.key));
                        center.setField(Fitness_Center.Field.ADDRESS, received.get(Protocol.Field.ADDRESS.key));
                        if (received.get(Protocol.Field.ADDRESS_SECOND.key) != null) {
                            center.setField(Fitness_Center.Field.ADDRESS_SECOND, received.get(Protocol.Field.ADDRESS_SECOND.key));
                        }
                        center.setField(Fitness_Center.Field.ZIP_CODE, received.get(Protocol.Field.ZIP_CODE.key));
                        center.setField(Fitness_Center.Field.CITY, received.get(Protocol.Field.CITY.key));
                        if (received.get(Protocol.Field.PHONE.key) != null) {
                            center.setField(Fitness_Center.Field.PHONE, received.get(Protocol.Field.PHONE.key));
                        }
                        Database.update_entity(Database.Collections.Fitness_Centers, center);
                        manager.setField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));
                        Database.update_entity(Database.Collections.Fitness_Center_Managers, manager);

                        DisplayConfiguration configuration = (DisplayConfiguration) Database.new_entity(Database.Collections.DisplayConfigurations);
                        configuration.setField(DisplayConfiguration.Field.SHOW_EVENTS, false);
                        configuration.setField(DisplayConfiguration.Field.SELECTED_EVENTS, new ArrayList());
                        configuration.setField(DisplayConfiguration.Field.SHOW_NEWS, false);
                        configuration.setField(DisplayConfiguration.Field.NEWS_TYPE, "");
                        configuration.setField(DisplayConfiguration.Field.SHOW_GLOBAL_PERFORMANCES, false);
                        configuration.setField(DisplayConfiguration.Field.PERFORMANCES_TYPE, "");
                        configuration.setField(DisplayConfiguration.Field.SHOW_RANKING_DISCIPLINE, false);
                        configuration.setField(DisplayConfiguration.Field.RANKING_DISCIPLINE_TYPE, "");
                        configuration.setField(DisplayConfiguration.Field.SHOW_GLOBAL_RANKING, false);
                        configuration.setField(DisplayConfiguration.Field.SHOW_NATIONAL_PRODUCTION_RANKING, false);
                        Database.update_entity(Database.Collections.DisplayConfigurations, configuration);

                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_REG_SUCCESS.code);
                    } else {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.CTR_ERROR_ALREADY_EXISTS.code);
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
