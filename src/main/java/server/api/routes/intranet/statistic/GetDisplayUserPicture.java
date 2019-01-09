package server.api.routes.intranet.statistic;

import Tools.LogManager;
import com.mongodb.BasicDBObject;
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

import java.util.Map;

public class GetDisplayUserPicture {
    public GetDisplayUserPicture(Router router) {
        router.route(HttpMethod.POST, "/get/display/user/picture").handler(routingContext -> {
            Map<String, Object> received = routingContext.getBodyAsJson().getMap();
            ResponseObject sending;
            HttpServerResponse response = routingContext.response().putHeader("content-type", "text/plain");

            try {
                MongoCollection fitnessCenterCollection = Database.collections.get(Database.Collections.Pictures);
                Document center = new Fitness_Center((Document) fitnessCenterCollection.find(new BasicDBObject("_id", new ObjectId((String) received.get("id")))).first());
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
