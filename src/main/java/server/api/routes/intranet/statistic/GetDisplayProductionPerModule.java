package server.api.routes.intranet.statistic;

import Tools.LogManager;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Fitness_Center;
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.util.*;

public class GetDisplayProductionPerModule {
    public GetDisplayProductionPerModule(Router router) {
        router.route(HttpMethod.POST, "/get/display/ppp").handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            try {
                MongoCollection fitnessCenterCollection = Database.collections.get(Database.Collections.Modules);
                AggregateIterable iterable =  fitnessCenterCollection.aggregate(Arrays.asList(
                        Aggregates.match(new BasicDBObject("fitness_center_id", new ObjectId((String) received.get("id")))),
                        Aggregates.lookup("electricproductions", "_id", "module_id", "electricproductions"),
                        Aggregates.unwind("$electricproductions"),
                        Aggregates.group(new BasicDBObject("_id", "$_id"), Accumulators.push("electricproductions", "$electricproductions"))));
                Iterator iterator = iterable.iterator();
                List<String> results = new ArrayList<>();
                while (iterator.hasNext()) {
                    System.out.println(doc.get("_id"));
                    Document doc = (Document) iterator.next();
                    ObjectId id = (ObjectId) doc.get("_id");
                    doc.append("_id", id.toString());
                    results.add(doc.toJson());
                }
                response.end(new GsonBuilder().create().toJson(results));

            } catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
        });

    }

    /*

#get_best_production_year
# The best production of a user
db.electricproductions.find({}).sort({production_year: -1}).limit(3)

#get_total_production_year
# Of the fitness center, Get all productions and (display the calculation of their sum)
db.electricproductions.find({})
     */

    public static void main(String[] args) {
        MongoCollection fitnessCenterCollection = Database.collections.get(Database.Collections.Modules);
        AggregateIterable iterable =  fitnessCenterCollection.aggregate(Arrays.asList(
                Aggregates.match(new BasicDBObject("fitness_center_id", new ObjectId("5be823d310dc3238eeed16a7"))),
                Aggregates.lookup("electricproductions", "_id", "module_id", "electricproductions"),
                Aggregates.unwind("$electricproductions"),
                Aggregates.group(new BasicDBObject("_id", "$_id"), Accumulators.push("electricproductions", "$electricproductions"))));
        Iterator iterator = iterable.iterator();
        List<String> results = new ArrayList<>();
        while (iterator.hasNext()) {
            Document doc = (Document) iterator.next();
            results.add(doc.toJson());
        }
        System.out.println();
    }

}
