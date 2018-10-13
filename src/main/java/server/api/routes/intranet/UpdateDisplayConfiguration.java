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
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class UpdateDisplayConfiguration {
    public UpdateDisplayConfiguration(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.UPDATE_DISPLAY_CONFIGURATION.path).handler(routingContext -> {
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
                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                        DisplayConfiguration configuration = (DisplayConfiguration) Database.find_entity(Database.Collections.DisplayConfigurations, DisplayConfiguration.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));

                        @SuppressWarnings("unchecked")
                        ArrayList<String> selected_array = (ArrayList<String>) received.get(Protocol.Field.SELECTED_EVENTS.key);
                        ArrayList<ObjectId> selected_events_to_insert = new ArrayList<>();

                        for (String cur :selected_array) {
                            selected_events_to_insert.add(new ObjectId(cur));
                        }

                        configuration.setField(DisplayConfiguration.Field.SHOW_EVENTS, received.get(Protocol.Field.SHOW_EVENTS.key));
                        configuration.setField(DisplayConfiguration.Field.SELECTED_EVENTS, selected_events_to_insert);
                        configuration.setField(DisplayConfiguration.Field.SHOW_NEWS, received.get(Protocol.Field.SHOW_NEWS.key));
                        configuration.setField(DisplayConfiguration.Field.NEWS_TYPE, received.get(Protocol.Field.NEWS_TYPE.key));
                        configuration.setField(DisplayConfiguration.Field.SHOW_GLOBAL_PERFORMANCES, received.get(Protocol.Field.SHOW_GLOBAL_PERFORMANCES.key));
                        configuration.setField(DisplayConfiguration.Field.PERFORMANCES_TYPE, received.get(Protocol.Field.PERFORMANCES_TYPE.key));
                        configuration.setField(DisplayConfiguration.Field.SHOW_RANKING_DISCIPLINE, received.get(Protocol.Field.SHOW_RANKING_DISCIPLINE.key));
                        configuration.setField(DisplayConfiguration.Field.RANKING_DISCIPLINE_TYPE, received.get(Protocol.Field.RANKING_DISCIPLINE_TYPE.key));
                        configuration.setField(DisplayConfiguration.Field.SHOW_GLOBAL_RANKING, received.get(Protocol.Field.SHOW_GLOBAL_RANKING.key));
                        configuration.setField(DisplayConfiguration.Field.SHOW_NATIONAL_PRODUCTION_RANKING, received.get(Protocol.Field.SHOW_NATIONAL_PRODUCTION_RANKING.key));
                        Database.update_entity(Database.Collections.DisplayConfigurations, configuration);
                    }
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().create().toJson(sending));
        });

    }
}
