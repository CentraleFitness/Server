import Tools.LogManager;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.vertx.ext.web.handler.FormLoginHandler;
import model.Database;
import model.entities.Feedback;
import model.entities.User;
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.mobile.ResponseObject;

import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
            LogManager.enable();
            LogManager.write("toto");

            System.out.println(new Date().toString());
        }
    }

    public static class TestGetField {
        public static void main(String[] args) {
            Map<String, LinkedList<Integer>> map1 = new HashMap<>();
            LinkedList<Integer> list1 = new LinkedList<Integer>();

            list1.push(0);
            list1.push(1);

            map1.put("list1", list1);

            for (Integer i : map1.get("list1")) {
                System.out.println(i);
            }

            map1.get("list1").push(4);

            for (Integer i : map1.get("list1")) {
                System.out.println(i);
            }
        }
    }

    public static class TestDB {
        public static void main(String[] args) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            Database db = Database.getInstance();

            LinkedList<Database.Entity> feedBacks = db.find_entities(Database.Collections.Feedbacks, Feedback.Field.ID, "");
            Feedback feedback = (Feedback) feedBacks.getFirst();
        }
    }
}
