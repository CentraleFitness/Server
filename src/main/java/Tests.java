import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.Database;
import org.bson.Document;
import protocol.mobile.ResponseObject;

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

            Database.User user = new Database.User(users.find(eq("login", "daures_h")).first());

            user.setToken(null);
            user.setPasswordHash(null);

            users.updateOne(eq("login", "daures_h"), new Document("$set", user.getDoc()));

            user = new Database.User();
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
            System.out.println(test.get("excrement"));
        }
    }
}
