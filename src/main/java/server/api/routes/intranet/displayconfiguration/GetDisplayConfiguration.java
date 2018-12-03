package server.api.routes.intranet.displayconfiguration;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.client.model.Filters;
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

public class GetDisplayConfiguration {
    public GetDisplayConfiguration(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.GET_DISPLAY_CONFIGURATION.path).handler(routingContext -> {
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

                        if (Database.collections.get(Database.Collections.DisplayConfigurations).countDocuments(Filters.eq("fitness_center_id", center.getField(Fitness_Center.Field.ID))) == 0) {
                            DisplayConfiguration configuration = (DisplayConfiguration) Database.new_entity(Database.Collections.DisplayConfigurations);
                            configuration.setField(DisplayConfiguration.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));
                            configuration.setField(DisplayConfiguration.Field.SHOW_EVENTS, false);
                            configuration.setField(DisplayConfiguration.Field.SELECTED_EVENTS, new ArrayList());
                            configuration.setField(DisplayConfiguration.Field.SHOW_NEWS, false);
                            configuration.setField(DisplayConfiguration.Field.NEWS_TYPE, new ArrayList());
                            configuration.setField(DisplayConfiguration.Field.SHOW_GLOBAL_PERFORMANCES, false);
                            configuration.setField(DisplayConfiguration.Field.PERFORMANCES_TYPE, "");
                            configuration.setField(DisplayConfiguration.Field.SHOW_RANKING_DISCIPLINE, false);
                            configuration.setField(DisplayConfiguration.Field.RANKING_DISCIPLINE_TYPE, "");
                            configuration.setField(DisplayConfiguration.Field.SHOW_GLOBAL_RANKING, false);
                            configuration.setField(DisplayConfiguration.Field.SHOW_NATIONAL_PRODUCTION_RANKING, false);
                            Database.update_entity(Database.Collections.DisplayConfigurations, configuration);
                        }

                        DisplayConfiguration configuration = (DisplayConfiguration) Database.find_entity(Database.Collections.DisplayConfigurations, DisplayConfiguration.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));

                        sending.put(Protocol.Field.SHOW_EVENTS.key, configuration.getField(DisplayConfiguration.Field.SHOW_EVENTS));
                        sending.put(Protocol.Field.SELECTED_EVENTS.key, configuration.getField(DisplayConfiguration.Field.SELECTED_EVENTS));
                        sending.put(Protocol.Field.SHOW_NEWS.key, configuration.getField(DisplayConfiguration.Field.SHOW_NEWS));
                        sending.put(Protocol.Field.NEWS_TYPE.key, configuration.getField(DisplayConfiguration.Field.NEWS_TYPE));
                        sending.put(Protocol.Field.SHOW_GLOBAL_PERFORMANCES.key, configuration.getField(DisplayConfiguration.Field.SHOW_GLOBAL_PERFORMANCES));
                        sending.put(Protocol.Field.PERFORMANCES_TYPE.key, configuration.getField(DisplayConfiguration.Field.PERFORMANCES_TYPE));
                        sending.put(Protocol.Field.SHOW_RANKING_DISCIPLINE.key, configuration.getField(DisplayConfiguration.Field.SHOW_RANKING_DISCIPLINE));
                        sending.put(Protocol.Field.RANKING_DISCIPLINE_TYPE.key, configuration.getField(DisplayConfiguration.Field.RANKING_DISCIPLINE_TYPE));
                        sending.put(Protocol.Field.SHOW_GLOBAL_RANKING.key, configuration.getField(DisplayConfiguration.Field.SHOW_GLOBAL_RANKING));
                        sending.put(Protocol.Field.SHOW_NATIONAL_PRODUCTION_RANKING.key, configuration.getField(DisplayConfiguration.Field.SHOW_NATIONAL_PRODUCTION_RANKING));
                    }
                }
            }catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdSerializer()).create().toJson(sending));
        });

    }
}
