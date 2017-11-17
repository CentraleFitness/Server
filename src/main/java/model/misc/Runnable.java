package model.misc;

import com.mongodb.client.MongoCollection;
import model.Database;
import model.entities.User;
import model.entities._IDS_;
import org.bson.Document;
import protocol.Protocol;
import server.misc.PasswordAuthentication;
import server.misc.Token;

import java.math.BigInteger;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;

public class Runnable {
    public static class new__IDS_ {
        public static void main(String[] args) {
            Database db = new Database();
            MongoCollection idss = db.collections.get(Database.Collections._IDS_);
            if (idss.count() == 0) {
                _IDS_ ids = new _IDS_();
                idss.insertOne(new Document(ids));
            } else {
                _IDS_ ids = new _IDS_((Document) idss.find().first());
                idss.updateOne(eq("_id", ids.get("_id")), new Document("$set", ids));
            }
        }
    }

    public static class new_User {
        public static void main(String[] args) {
            Database db = new Database();
            for (Database.Collections col : Database.Collections.values()) db.new_entity(col);
        }
    }

    public static class find_User {
        public static void main(String[] args) {
            Database db = new Database();
            User user = (User) db.find_entity(Database.Collections.Users, User.Field.LOGIN, "tata");
            user.setField(User.Field.EMAIL, "tata@gmail.com");
            System.out.println(user.getField(User.Field.EMAIL));
        }
    }
}
