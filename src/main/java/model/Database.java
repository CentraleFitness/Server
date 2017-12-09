package model;

import com.mongodb.MongoClient;
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
import java.util.Map;

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
        _IDS_("_IDS_", model.entities._IDS_.class, "IDS"),
        Users("users", model.entities.User.class, User.Field.USER_ID.get_key()),
        Modules("modules", model.entities.Module.class, Module.Field.MODULE_ID.get_key()),
        ElectricProductions("electricproductions", model.entities.ElectricProduction.class, ElectricProduction.Field.ELECTRIC_PRODUCTION_ID.get_key()),
        Events("events", model.entities.Event.class, Event.Field.EVENT_ID.get_key()),
        Conversations("conversations", model.entities.Conversation.class, Conversation.Field.CONVERSATION_ID.get_key()),
        Pictures("pictures", model.entities.Picture.class, Picture.Field.PICTURE_ID.get_key()),
        Fitness_Centers("fitness_centers", model.entities.Fitness_Center.class, Fitness_Center.Field.FITNESS_CENTER_ID.get_key()),
        Fitness_Center_Managers("fitness_center_managers", model.entities.Fitness_Center_Manager.class, Fitness_Center_Manager.Field.FITNESS_CENTER_MANAGER_ID.get_key()),
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
                ;
            @Override
            public String get_key() {
                return null;
            }

            @Override
            public Class get_class() {
                return null;
            }
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
        if (this.collections.get(Collections._IDS_).find().first() == null)
            this.collections.get(Collections._IDS_).insertOne(new Document(new model.entities._IDS_()));
    }

    public static Database getInstance() {return INSTANCE;}

    public Document new_entity(Collections collection) throws IllegalAccessException, InstantiationException {
        if (collection == Collections._IDS_) return null;
        try {
            Document doc = (Document) collection._class.newInstance();
            MongoCollection entity_collection = this.collections.get(collection);
            MongoCollection idss = this.collections.get(Database.Collections._IDS_);
            _IDS_ ids = new _IDS_((Document) idss.find().first());
            BigInteger id = new BigInteger((String) ids.get(collection.entity_id));
            id = id.add(BigInteger.ONE);
            doc.put(collection.entity_id, id.toString());
            entity_collection.insertOne(new Document(doc));
            idss.updateOne(eq("_id", ids.get("_id")), set(collection.entity_id, id.toString()));
            return doc;
        } catch (Exception e) {
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
            return (Document) c.newInstance(this.collections.get(collection).find(eq(field.get_key(), field.get_class().cast(value))).first());
        } catch (Exception e) {
            throw e;
        }
    }
}
