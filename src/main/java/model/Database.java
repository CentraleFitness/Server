package model;

import Tools.LogManager;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.entities.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

/**
 * Created by hadrien on 14/03/2017.
 */
public class Database {

    public MongoClient client = null;
    public MongoDatabase db = null;
    public Map<Collections, MongoCollection> collections = null;

    public static String ip = "localhost";
    public static int port = 27017;
    public static String name = "centralefitness";
    public static String idKey = "_id";

    private static Database INSTANCE = new Database();

    public enum Collections {
        Users("users", model.entities.User.class, User.Field.ID.get_key()),
        Modules("modules", model.entities.Module.class, Module.Field.ID.get_key()),
        ElectricProductions("electricproductions", model.entities.ElectricProduction.class, ElectricProduction.Field.ID.get_key()),
        Events("events", model.entities.Event.class, Event.Field.ID.get_key()),
        Conversations("conversations", model.entities.Conversation.class, Conversation.Field.ID.get_key()),
        Pictures("pictures", model.entities.Picture.class, Picture.Field.ID.get_key()),
        Fitness_Centers("fitness_centers", model.entities.Fitness_Center.class, Fitness_Center.Field.ID.get_key()),
        Fitness_Center_Managers("fitness_center_managers", model.entities.Fitness_Center_Manager.class, Fitness_Center_Manager.Field.ID.get_key()),
        ;

        public String key;
        public Class<?> _class;
        public String entity_id;
        Collections(String key, Class<?> _class, String entity_id) {
            this.key = key;
            this._class = _class;
            this.entity_id = entity_id;}
    }

    public interface Entity_Field {
        String get_key();
        Class get_class();
    }

    public static abstract class Entity extends Document {
        public enum Field implements Entity_Field {
            ID("_id", ObjectId.class),
            ;
            @Override
            public String get_key() {
                return this.key;
            }

            @Override
            public Class get_class() {
                return this._class;
            }
            private String key;
            private Class _class;
            Field(String key, Class _class) {this.key = key; this._class = _class;}
        }

        public Object getField(Entity_Field field) {
            return get(field.get_key());
        }

        public void setField(Entity_Field field, Object value) {
            put(field.get_key(), field.get_class().cast(value));
        }

        public Entity() {for (Field field : Field.values())
            try {
                setField(field, field.get_class().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Entity(Document doc) {super(doc);}
    }

    private Database() {
        this.client = new MongoClient(Database.ip, Database.port);
        this.db = this.client.getDatabase(Database.name);
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
    }

    public static Database getInstance() {return INSTANCE;}

    public Document new_entity(Collections collection) throws IllegalAccessException, InstantiationException {
        try {
            Document doc = (Document) collection._class.newInstance();
            MongoCollection entity_collection = this.collections.get(collection);
            ObjectId id = new ObjectId();
            doc.put("_id", id);
            doc.put(collection.entity_id, id);
            entity_collection.insertOne(new Document(doc));
            return doc;
        } catch (Exception e) {
            LogManager.write("new_entity error");
            throw e;
        }
    }

    public void update_entity(Collections collection, Document entity) {
        this.collections.get(collection).updateOne(eq(collection.entity_id, entity.get(collection.entity_id)), new Document("$set", entity));
    }

    public Document find_entity(Collections collection, Entity_Field field, Object value) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Constructor c = null;
        try {
            c = collection._class.getConstructor(Document.class);
            Document entity = (Document) this.collections.get(collection).find(eq(field.get_key(), field.get_class().cast(value))).first();
            return (entity != null ? (Document) c.newInstance(entity) : entity);
        } catch (Exception e) {
            LogManager.write("find_entity error");
            throw e;
        }
    }

    public LinkedList<Entity> find_entities(Collections collection, Entity_Field field, Object value) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Constructor c = null;
        try {
            LinkedList entities = new LinkedList<Document>();
            c = collection._class.getConstructor(Document.class);
            for (Document doc : (FindIterable<Document>) this.collections.get(collection).find(eq(field.get_key(), field.get_class().cast(value)))) {
                entities.push(c.newInstance(doc));
            }
            return entities;
        } catch (Exception e) {
            LogManager.write("find_entity error");
            throw e;
        }
    }

    public void delete_entity(Collections collection, Entity_Field field, Object value) {
        this.collections.get(collection).deleteOne(eq(field.get_key(), field.get_class().cast(value)));
    }

    public void delete_entities(Collections collection, Entity_Field field, Object value) {
        this.collections.get(collection).deleteMany(eq(field.get_key(), field.get_class().cast(value)));
    }
}
