package server.api.routes.intranet;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import model.entities.Statistic;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.util.Map;
import java.util.Objects;

public class GetStatistics {
    public GetStatistics(Router router) {
        router.route(HttpMethod.POST, Protocol.Path.GET_STATISTICS.path).handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");
            Fitness_Center_Manager manager;

            try {
                manager = (Fitness_Center_Manager) Database.find_entity(Database.Collections.Fitness_Center_Managers, Fitness_Center_Manager.Field.EMAIL, Token.decodeToken((String) received.get(Protocol.Field.TOKEN.key)).getIssuer());
                if (!Objects.equals(manager.getField(Fitness_Center_Manager.Field.TOKEN), received.get(Protocol.Field.TOKEN.key))) {
                    sending = new ResponseObject(true);
                    sending.put(Protocol.Field.STATUS.key, Protocol.Status.AUTH_ERROR_TOKEN.code);
                } else {
                    Fitness_Center center = (Fitness_Center) Database.find_entity(Database.Collections.Fitness_Centers, Fitness_Center.Field.ID, manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID));

                    if (center == null) {
                        sending = new ResponseObject(true);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.MGR_ERROR_NO_CENTER.code);
                    } else {
                        sending = new ResponseObject(false);
                        sending.put(Protocol.Field.STATUS.key, Protocol.Status.GENERIC_OK.code);

                        Statistic statistic = (Statistic) Database.find_entity(Database.Collections.Statistics, Statistic.Field.FITNESS_CENTER_ID, center.getField(Fitness_Center.Field.ID));

                        sending.put(Protocol.Field.PRODUCTION_DAY.key, statistic.getField(Statistic.Field.PRODUCTION_DAY));
                        sending.put(Protocol.Field.PRODUCTION_MONTH.key, statistic.getField(Statistic.Field.PRODUCTION_MONTH));
                        sending.put(Protocol.Field.FREQUENTATION_DAY.key, statistic.getField(Statistic.Field.FREQUENTATION_DAY));
                        sending.put(Protocol.Field.FREQUENTATION_MONTH.key, statistic.getField(Statistic.Field.FREQUENTATION_MONTH));

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
