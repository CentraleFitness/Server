package server.api.routes.intranet.statistic;

import Tools.LogManager;
import Tools.ObjectIdSerializer;
import Tools.Token;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import model.Database;
import model.entities.ElectricProduction;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import model.entities.User;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import protocol.ResponseObject;
import protocol.intranet.Protocol;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class GetCenterId {
    public GetCenterId(Router router) {
        router.route(HttpMethod.POST, "/get/display/center").handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            try {
                MongoCollection fitnessCenterCollection = Database.collections.get(Database.Collections.Fitness_Centers);
                Document center = new Fitness_Center((Document) fitnessCenterCollection.find(new BasicDBObject("apiKey", new ObjectId((String) received.get("id")))).first());
                response.end(center.toJson());

            } catch (Exception e){
                sending = new ResponseObject(true);
                sending.put(Protocol.Field.STATUS.key, Protocol.Status.MISC_ERROR.code);
                LogManager.write("Exception: " + e.toString());
            }
        });

    }

    public static void main(String[] args) {
        System.out.println();
    }

}
