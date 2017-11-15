package model.misc;

import com.mongodb.client.MongoCollection;
import model.Database;
import model.entities.User;
import model.entities._IDS_;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;

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
            MongoCollection users = db.collections.get(Database.Collections.Users);
            if (users.count() == 0) {
                User user = new User();
                users.insertOne(new Document(user));
            } else {
                _IDS_ ids = new _IDS_((Document) users.find().first());
                users.updateOne(eq("_id", ids.get("_id")), new Document("$set", ids));
            }
        }
    }
}
