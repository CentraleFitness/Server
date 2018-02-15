import Tools.LogManager;
import com.google.gson.*;
import io.vertx.ext.web.handler.FormLoginHandler;
import model.Database;
import model.entities.Feedback;
import model.entities.SportSession;
import model.entities.User;
import org.bson.types.ObjectId;
import protocol.ResponseObject;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

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
            Map map = new TreeMap();
            map.put("Test", 15f);
            map.compute("Test", (key, value) -> {
                System.out.println(key);
                System.out.println(value);
                return ((float)value + 1.0f);
            });
            System.out.println(map.get("Test"));
        }
    }

    public static class TestDB {
        public static void main(String[] args) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            LinkedList<Database.Entity> feedBacks = Database.find_entities(Database.Collections.Feedbacks, Feedback.Field.ID, "");
        }
    }

    public static class TestRandom {
        public static void main(String[] args) throws IllegalAccessException, InstantiationException {
            String str = new ObjectId().toString();
            ObjectId objid = new ObjectId(str);
            System.out.println(str);
            System.out.println(objid.toString());
        }
    }
}
