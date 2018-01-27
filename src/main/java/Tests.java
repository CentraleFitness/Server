import Tools.LogManager;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.vertx.ext.web.handler.FormLoginHandler;
import model.Database;
import model.entities.User;
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.mobile.ResponseObject;

import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by hadrien on 15/03/2017.
 */
public class Tests {



    public static class Test2 {
        public static void main(String[] args) {
            ResponseObject test = new ResponseObject(false);
            test.put("login", "test login");
            test.put("password", "test password");
            System.out.println(new Gson().toJson(test));

            FormLoginHandler toto;
        }
    }


    public static class Test5 {
        public static void main(String[] args) {
            String json = "{\"Success\":true,\"Message\":\"Invalid access token.\"}";
            JsonParser jsonParser = new JsonParser();
            JsonObject jo = (JsonObject)jsonParser.parse(json);
            System.out.println(jo.get("Message"));
        }
    }

    public static class TestLogManager {
        public static void main(String[] args) {
            LogManager.write("toto");

            System.out.println(new Date().toString());
        }
    }
}
