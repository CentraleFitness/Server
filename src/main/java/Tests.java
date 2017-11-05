import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.vertx.ext.web.handler.FormLoginHandler;
import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.mobile.ResponseObject;

import java.io.FileReader;
import java.util.HashMap;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by hadrien on 15/03/2017.
 */
public class Tests {

    public static class Test1 {

        /**
         * Creation d'un user dans la DB
         */
        public static void main(String[] args) {

            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
            MongoDatabase database = mongoClient.getDatabase("centralefitness");
            MongoCollection<Document> users = database.getCollection("users");

            model.entities.User user = new model.entities.User(users.find(eq("login", "daures_h")).first());

            try {
                user.setToken(null);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            user.setPasswordHash(null);

            users.updateOne(eq("login", "daures_h"), new Document("$set", user.getDoc()));

            user = new model.entities.User();
            user.setLogin("Chaton_hardcore");
            users.insertOne(user.getDoc());
            users.find(eq("login", null)).first();
        }
    }

    public static class Test2 {
        public static void main(String[] args) {
            ResponseObject test = new ResponseObject(false);
            test.put("login", "test login");
            test.put("password", "test password");
            System.out.println(new Gson().toJson(test));

            FormLoginHandler toto;
        }
    }

    public static class Test3 {

        public static void main(String[] args) {
            Database database = new Database();
            model.entities.User user = new model.entities.User();
            model.entities.Module module = new model.entities.Module();
            Database.ElectricProduction ep = new Database.ElectricProduction();

            user.setLogin("psyycker");
            user.setPasswordHash("$31$16$aGYGMXwIfSe-d7DY6ld1xHJkYrUeLkFFpSeQ5uC0D_0");
            database.users.insertOne(user.getDoc());
            user = new model.entities.User((Document) database.users.find(eq(model.entities.User.Fields.login, "psyycker")).first());

            module.setModuleName("module1");
            module.setMachineType("velo");
            module.setCurrentUser("psyycker");
            database.modules.insertOne(module.getDoc());
            module = new model.entities.Module((Document) database.modules.find(eq(model.entities.Module.Fields.moduleName, "module1")).first());

            user.getModules().put(module.getName(), (ObjectId) module.getDoc().get("_id"));
            database.users.updateOne(eq(model.entities.User.Fields.login, user.getLogin()), new Document("$set", user.getDoc()));
            module.getUsers().put(user.getLogin(), (ObjectId) user.getDoc().get("_id"));
            database.modules.updateOne(eq(model.entities.Module.Fields.moduleName, module.getName()), new Document("$set", module.getDoc()));

            ep.setModuleId((ObjectId) module.getDoc().get("_id"));
            ep.setUserId((ObjectId) user.getDoc().get("_id"));
            database.electricProductions.insertOne(ep.getDoc());
        }
    }

    public static class Test4 {
        public static void main(String[] args) {
            Database database = new Database();
            model.entities.User user = new model.entities.User((Document) database.users.find(eq(model.entities.User.Fields.login, "psyycker")).first());
            System.out.println(user.getId());
            System.out.println(database.users.getClass());
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
}
