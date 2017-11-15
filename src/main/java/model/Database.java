package model;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hadrien on 14/03/2017.
 */
public class Database {

    public MongoClient client = null;
    public MongoDatabase db = null;
    public MongoCollection users = null;
    public MongoCollection modules = null;
    public MongoCollection electricProductions = null;

    public static String ip = "localhost";
    public static int port = 27017;
    public static String name = "centralefitness";
    public static String idKey = "_id";

    public enum Collections {
        _IDS_("_IDS_"),
        Users("users"),
        Modules("modules"),
        ElectricProductions("electricproductions"),
        Events("events"),
        Conversations("conversations"),
        Pictures("pictures"),
        Fitness_Centers("fitness_centers"),
        Fitness_Center_Managers("fitness_center_managers"),
        ;

        public String key;
        Collections(String key) {this.key = key;}
    }

    public Database() {
        this.client = new MongoClient(Database.ip, Database.port);
        this.db = this.client.getDatabase(Database.name);
        this.users = this.db.getCollection(Collections.Users.key);
        this.modules = this.db.getCollection(Collections.Modules.key);
        this.electricProductions = this.db.getCollection(Collections.ElectricProductions.key);
}

    public static class DataDocument {
        public Document doc;
        public DataDocument() { this.doc = new Document();}
        public DataDocument(Document doc) {this.doc = doc;}
        public Document getDoc() {return this.doc;}
        public Document getUpdate() {return new Document("$set", this.doc);}
        public ObjectId getId() {return (ObjectId) this.doc.get("_id");}
    }

    public MongoCollection getCollection(Collections col) {
        MongoCollection collection;
        try {
            collection = this.db.getCollection(col.key);
        } catch (Exception e) {
            this.db.createCollection(col.key);
            collection = this.db.getCollection(col.key);
        }
        return collection;
    }
}
