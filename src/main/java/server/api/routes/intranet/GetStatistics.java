package server.api.routes.intranet;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.util.*;

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

                        Bson users_filter = Filters.and(
                                Filters.eq(User.Field.FITNESS_CENTER_ID.get_key(),
                                        manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID))
                        );

                        List<ObjectId> users = new ArrayList<>();
                        FindIterable<User> findIterableUsers = (FindIterable<User>) Database.collections.get(Database.Collections.Users).find(users_filter);
                        for (Document doc : findIterableUsers) {

                            users.add(doc.getObjectId("_id"));
                        }

                        Bson production_filter = Filters.and(
                                Filters.in(ElectricProduction.Field.USER_ID.get_key(), users)
                        );

                        Double production_day = 0d;
                        Double production_month = 0d;
                        Double production_year = 0d;
                        Double production_total = 0d;
                        HashMap<ObjectId, Double> modules = new HashMap<>();
                        Double total_production_modules = 0d;
                        FindIterable<ElectricProduction> findIterableProductions = (FindIterable<ElectricProduction>) Database.collections.get(Database.Collections.ElectricProductions).find(production_filter);
                        for (ElectricProduction doc : findIterableProductions) {

                            doc.updateProduction();

                            production_day += (Double)doc.getField(ElectricProduction.Field.PRODUCTION_DAY);
                            production_month += (Double)doc.getField(ElectricProduction.Field.PRODUCTION_MONTH);
                            production_year += (Double)doc.getField(ElectricProduction.Field.PRODUCTION_YEAR);
                            production_total += (Double)doc.getField(ElectricProduction.Field.PRODUCTION_TOTAL);

                            total_production_modules += (Double)doc.getField(ElectricProduction.Field.PRODUCTION_TOTAL);
                            modules.put((ObjectId) doc.getField(ElectricProduction.Field.MODULE_ID), 0d);
                        }

                        Double day_duration = (24d * (60d * 60d * 1000d));
                        Bson users_day_filter = Filters.and(
                                Filters.gte(User.Field.CREATION_DATE.get_key(),
                                        System.currentTimeMillis() - day_duration),
                                Filters.eq(User.Field.FITNESS_CENTER_ID.get_key(),
                                        manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID))
                        );

                        List<ObjectId> users_day = new ArrayList<>();
                        findIterableUsers = (FindIterable<User>) Database.collections.get(Database.Collections.Users).find(users_day_filter);
                        for (Document doc : findIterableUsers) {

                            users_day.add(doc.getObjectId("_id"));
                        }

                        Bson users_month_filter = Filters.and(
                                Filters.gte(User.Field.CREATION_DATE.get_key(),
                                        System.currentTimeMillis() - (day_duration * 31)),
                                Filters.eq(User.Field.FITNESS_CENTER_ID.get_key(),
                                        manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID))
                        );

                        List<ObjectId> users_month = new ArrayList<>();
                        findIterableUsers = (FindIterable<User>) Database.collections.get(Database.Collections.Users).find(users_month_filter);
                        for (Document doc : findIterableUsers) {

                            users_month.add(doc.getObjectId("_id"));
                        }

                        Bson users_year_filter = Filters.and(
                                Filters.gte(User.Field.CREATION_DATE.get_key(),
                                        System.currentTimeMillis() - (day_duration * 365)),
                                Filters.eq(User.Field.FITNESS_CENTER_ID.get_key(),
                                        manager.getField(Fitness_Center_Manager.Field.FITNESS_CENTER_ID))
                        );

                        List<ObjectId> users_year = new ArrayList<>();
                        findIterableUsers = (FindIterable<User>) Database.collections.get(Database.Collections.Users).find(users_year_filter);
                        for (Document doc : findIterableUsers) {

                            users_year.add(doc.getObjectId("_id"));
                        }

                        sending.put(Protocol.Field.PRODUCTION_DAY.key, production_day);
                        sending.put(Protocol.Field.PRODUCTION_MONTH.key, production_month);
                        sending.put(Protocol.Field.PRODUCTION_YEAR.key, production_year);
                        sending.put(Protocol.Field.PRODUCTION_TOTAL.key, production_total);
                        sending.put(Protocol.Field.AVERAGE_BY_MODULE.key, (modules.size() == 0 ? 0 : total_production_modules / modules.size()));

                        sending.put(Protocol.Field.FREQUENTATION_DAY.key, users_day.size());
                        sending.put(Protocol.Field.FREQUENTATION_MONTH.key, users_month.size());
                        sending.put(Protocol.Field.FREQUENTATION_YEAR.key, users_year.size());

                        sending.put(Protocol.Field.NB_SUBSCRIBERS.key, users.size());

                    }
                }
            } catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
            response.end(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdSerializer()).create().toJson(sending));
        });

    }
}
