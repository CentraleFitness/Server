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
            MongoCollection users = db.collections.get(Database.Collections.Users);
            MongoCollection idss = db.collections.get(Database.Collections._IDS_);
            _IDS_ ids = new _IDS_((Document) idss.find().first());
            BigInteger id = new BigInteger((String) ids.get(_IDS_.Fields.last_User_id));
            id = id.add(BigInteger.ONE);
            User user = new User();
            user.put(User.Fields.user_id, id.toString());
            users.insertOne(new Document(user));
            idss.updateOne(eq("_id", ids.get("_id")), set(_IDS_.Fields.last_User_id, id.toString()));
        }
    }
}
