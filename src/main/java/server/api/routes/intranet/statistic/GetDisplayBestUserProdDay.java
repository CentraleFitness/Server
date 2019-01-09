package server.api.routes.intranet.statistic;

import Tools.LogManager;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.Fitness_Center;
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GetDisplayBestUserProdDay {
    public GetDisplayBestUserProdDay(Router router) {
        router.route(HttpMethod.POST, "/get/display/user/best/day").handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            try {
                MongoCollection fitnessCenterCollection = Database.collections.get(Database.Collections.ElectricProductions);
                FindIterable center = fitnessCenterCollection.find().sort(new BasicDBObject("production_day", -1)).limit(3);
                List<String> results = new ArrayList<>();
                Iterator it = center.iterator();
                while (it.hasNext()) {
                    Document doc = (Document) it.next();
                    results.add(doc.toJson());
                }
                response.end(new GsonBuilder().create().toJson(results));

            } catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
        });

        /*
            #get_best_production_day
# The best production of a user
db.electricproductions.find({}).sort({production_day: -1}).limit(3)

         */

    }

    public static void main(String[] args) {
        MongoCollection fitnessCenterCollection = Database.collections.get(Database.Collections.ElectricProductions);
        FindIterable center = fitnessCenterCollection.find().sort(new BasicDBObject("production_day", -1)).limit(3);
        System.out.println();
    }

}
