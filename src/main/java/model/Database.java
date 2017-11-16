package model;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.entities.Module;
import model.entities.User;
import model.entities._IDS_;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

/**
 * Created by hadrien on 14/03/2017.
 */
public class Database {

    public MongoClient client = null;
    public MongoDatabase db = null;
    public MongoCollection users = null;
    public MongoCollection modules = null;
    public MongoCollection electricProductions = null;
    public Map<Collections, MongoCollection> collections = null;

    public static String ip = "localhost";
    public static int port = 27017;
    public static String name = "centralefitness";
    public static String idKey = "_id";

    public enum Collections {
        _IDS_("_IDS_", model.entities._IDS_.class, null),
        Users("users", model.entities.User.class, User.Fields.user_id),
        Modules("modules", model.entities.Module.class, ""),
        ElectricProductions("electricproductions", model.entities.ElectricProduction.class, ""),
        Events("events", model.entities.Event.class, ""),
        Conversations("conversations", model.entities.Conversation.class, ""),
        Pictures("pictures", model.entities.Picture.class, ""),
        Fitness_Centers("fitness_centers", model.entities.Fitness_Center.class, ""),
        Fitness_Center_Managers("fitness_center_managers", model.entities.Fitness_Center_Manager.class, ""),
        ;

        public String key;
        public Class<?> _class;
        public String entity_id;
        Collections(String key, Class<?> _class, String entity_id) {
            this.key = key;
            this._class = _class;
            this.entity_id = entity_id;}
    }

    public Database() {
        this.client = new MongoClient(Database.ip, Database.port);
        this.db = this.client.getDatabase(Database.name);
        this.users = this.db.getCollection(Collections.Users.key);
        this.modules = this.db.getCollection(Collections.Modules.key);
        this.electricProductions = this.db.getCollection(Collections.ElectricProductions.key);
        this.collections = new HashMap<>();
        for (Collections col : Collections.values()) {
            MongoCollection collection;
            try {
                collection = db.getCollection(col.key);
            } catch (Exception e) {
                db.createCollection(col.key);
                collection = db.getCollection(col.key);
            }
            this.collections.put(col, collection);
        }
        if (this.collections.get(Collections._IDS_).find().first() == null)
            this.collections.get(Collections._IDS_).insertOne(new Document(new model.entities._IDS_()));
    }

    public Document new_entity(Collections collection) {
        if (collection == Collections._IDS_) return null;
        try {
            Document doc = (Document) collection._class.newInstance();
            MongoCollection entity_collection = this.collections.get(collection);
            MongoCollection idss = this.collections.get(Database.Collections._IDS_);
            _IDS_ ids = new _IDS_((Document) idss.find().first());
            BigInteger id = new BigInteger((String) ids.get(collection.entity_id));
            id = id.add(BigInteger.ONE);
            doc.put(User.Fields.user_id, id.toString());
            users.insertOne(new Document(doc));
            idss.updateOne(eq("_id", ids.get("_id")), set(collection.entity_id, id.toString()));
            return doc;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void update_entity(Collections collection, Document entity) {
        this.collections.get(collection).updateOne(eq(collection.entity_id, entity.get(collection.entity_id)), new Document("$set", entity));
    }

    public static class DataDocument {
        public Document doc;
        public DataDocument() { this.doc = new Document();}
        public DataDocument(Document doc) {this.doc = doc;}
        public Document getDoc() {return this.doc;}
        public Document getUpdate() {return new Document("$set", this.doc);}
        public ObjectId getId() {return (ObjectId) this.doc.get("_id");}
    }
}
