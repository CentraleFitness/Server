package model.misc;

import com.mongodb.client.MongoCollection;
import model.Database;
import model.entities._IDS_;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;

public class Runnable {
    public static class new__IDS_ {
        public static void main(String[] args) {
            Database db = new Database();
            MongoCollection col = db.getCollection(Database.Collections._IDS_);
            if (col.count() == 0) {
                _IDS_ ids = new _IDS_();
                col.insertOne(new Document(ids));
            } else {
                _IDS_ ids = new _IDS_((Document) col.find().first());
                col.updateOne(eq("_id", ids.get("_id")), new Document("$set", ids));
            }
        }
    }
}
