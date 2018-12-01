package model;

import static com.mongodb.client.model.Filters.eq;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import Tools.LogManager;
import Tools.PasswordAuthentication;
import Tools.Token;
import model.entities.Activity;
import model.entities.Administrator;
import model.entities.Challenge;
import model.entities.Conversation;
import model.entities.CustomProgram;
import model.entities.DisplayConfiguration;
import model.entities.ElectricProduction;
import model.entities.Event;
import model.entities.Feedback;
import model.entities.Feedback_State;
import model.entities.Fitness_Center;
import model.entities.Fitness_Center_Manager;
import model.entities.MobileFeedback;
import model.entities.Module;
import model.entities.ModuleState;
import model.entities.Picture;
import model.entities.Post;
import model.entities.SportSession;
import model.entities.Statistic;
import model.entities.TUPLE_Event_User;
import model.entities.User;

/**
 * Created by hadrien on 14/03/2017.
 */
public class Database {

    private static MongoClient client = null;
    private static MongoDatabase db = null;
    public static Map<Collections, MongoCollection> collections = null;

    private static String ip = "localhost";
    private static int port = 27017;
    private static String name = "centralefitness";
    private static String idKey = "_id";

    private static Database INSTANCE = new Database();

    public enum Collections {
        Administrators("administrators", model.entities.Administrator.class, Administrator.Field.ID.get_key()),
        Users("users", model.entities.User.class, User.Field.ID.get_key()),
        Modules("modules", model.entities.Module.class, Module.Field.ID.get_key()),
        ElectricProductions("electricproductions", model.entities.ElectricProduction.class, ElectricProduction.Field.ID.get_key()),
        Events("events", model.entities.Event.class, Event.Field.ID.get_key()),
        Conversations("conversations", model.entities.Conversation.class, Conversation.Field.ID.get_key()),
        Pictures("pictures", model.entities.Picture.class, Picture.Field.ID.get_key()),
        Fitness_Centers("fitness_centers", model.entities.Fitness_Center.class, Fitness_Center.Field.ID.get_key()),
        Fitness_Center_Managers("fitness_center_managers", model.entities.Fitness_Center_Manager.class, Fitness_Center_Manager.Field.ID.get_key()),
        MobileFeedbacks("mobilefeedbacks", model.entities.MobileFeedback.class, MobileFeedback.Field.ID.get_key()),
        Feedbacks("feedbacks", model.entities.Feedback.class, Feedback.Field.ID.get_key()),
        Feedback_States("feedback_states", model.entities.Feedback_State.class, Feedback_State.Field.ID.get_key()),
        ModuleStates("module_states", model.entities.ModuleState.class, ModuleState.Field.ID.get_key()),
        TUPLE_Event_Users("TUPLE_event_users", model.entities.TUPLE_Event_User.class, TUPLE_Event_User.Field.ID.get_key()),
        SportSessions("sportsession", model.entities.SportSession.class, SportSession.Field.ID.get_key()),
        SportSessions_HISTORY("sportsession_HISTORY", model.entities.SportSession.class, SportSession.Field.ID.get_key()),
        CustomPrograms("custom_programs", model.entities.CustomProgram.class, CustomProgram.Field.ID.get_key()),
        Activities("activities", model.entities.Activity.class, Activity.Field.ID.get_key()),
        DisplayConfigurations("display_configuration", model.entities.DisplayConfiguration.class, DisplayConfiguration.Field.ID.get_key()),
        Posts("posts", model.entities.Post.class, Post.Field.ID.get_key()),
        Challenges("challenges", model.entities.Challenge.class, Challenge.Field.ID.get_key()),
        Statistics("statistics", model.entities.Statistic.class, Statistic.Field.ID.get_key()),
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
            ID("_id", ObjectId.class);

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

            Field(String key, Class _class) {
                this.key = key;
                this._class = _class;
            }
        }

        public Object getField(Entity_Field field) {
            return get(field.get_key());
        }
        
        public ObjectId getId() {
        	return (ObjectId) get("_id");
        }

        public void setField(Entity_Field field, Object value) {
            put(field.get_key(), field.get_class().cast(value));
        }

        public Entity() {
            setField(Field.ID, new ObjectId());
        }

        public Entity(Document doc) {
            super(doc);
        }
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

    @Deprecated
    public static Database getInstance() {return INSTANCE;}

    public static Document new_entity(Collections collection) throws IllegalAccessException, InstantiationException {
        try {
            Document doc = (Document) collection._class.newInstance();
            MongoCollection entity_collection = collections.get(collection);
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

    public static void update_entity(Collections collection, Document entity) {
        collections.get(collection).updateOne(eq(collection.entity_id, entity.get(collection.entity_id)), new Document("$set", entity));
    }

    public static void insert_entity(Collections collection, Document entity) {
        collections.get(collection).insertOne(new Document(entity));
    }

    public static Document find_entity(Collections collection, Entity_Field field, Object value) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Constructor c = null;
        try {
            c = collection._class.getConstructor(Document.class);
            Document entity = (Document) collections.get(collection).find(eq(field.get_key(), field.get_class().cast(value))).first();
            return (entity != null ? (Document) c.newInstance(entity) : entity);
        } catch (Exception e) {
            LogManager.write(e);
            throw e;
        }
    }

    public static Document find_entity(Collections collection, Bson filters) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Constructor c = null;
        try {
            c = collection._class.getConstructor(Document.class);
            Document entity = (Document) collections.get(collection).find(filters).first();
            return (entity != null ? (Document) c.newInstance(entity) : entity);
        } catch (Exception e) {
            LogManager.write(e);
            throw e;
        }
    }

    public static LinkedList<Entity> find_entities(Collections collection, Entity_Field field, Object value) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Constructor c = null;
        try {
            LinkedList entities = new LinkedList<Document>();
            c = collection._class.getConstructor(Document.class);
            for (Document doc : (FindIterable<Document>) collections.get(collection).find(eq(field.get_key(), field.get_class().cast(value)))) {
                entities.push(c.newInstance(doc));
            }
            return entities;
        } catch (Exception e) {
            LogManager.write(e);
            throw e;
        }
    }

    public static LinkedList<Entity> find_entities(Collections collection, Bson filters) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Constructor c = null;
        try {
            LinkedList entities = new LinkedList<Document>();
            c = collection._class.getConstructor(Document.class);
            for (Document doc : (FindIterable<Document>) collections.get(collection).find(filters)) {
                entities.push(c.newInstance(doc));
            }
            return entities;
        } catch (Exception e) {
            LogManager.write(e);
            throw e;
        }
    }

    public static long countEntities(Collections collection){

        return collections.get(collection).countDocuments();
    }

    public static void delete_entity(Collections collection, Entity_Field field, Object value) {
        collections.get(collection).deleteOne(eq(field.get_key(), field.get_class().cast(value)));
    }

    public static void delete_entity(Collections collection, Bson filters) {
        collections.get(collection).deleteOne(filters);
    }

    public static void delete_entities(Collections collection, Entity_Field field, Object value) {
        collections.get(collection).deleteMany(eq(field.get_key(), field.get_class().cast(value)));
    }

    public static void delete_entities(Collections collection, Bson filters) {
		collections.get(collection).deleteMany(filters);
	}

	static {
        if (collections.get(Collections.Activities).countDocuments() == 0) {
            try {

                Activity a = (Activity) new_entity(Collections.Activities);
                a.setField(Activity.Field.NAME, "Abdominaux");
                a.setField(Activity.Field.IS_MODULE, false);
                a.setField(Activity.Field.ICON, "data:image/png;base64,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");
                Database.update_entity(Collections.Activities, a);

                a = (Activity) new_entity(Collections.Activities);
                a.setField(Activity.Field.NAME, "Pompes");
                a.setField(Activity.Field.IS_MODULE, false);
                a.setField(Activity.Field.ICON, "data:image/png;base64,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");
                Database.update_entity(Collections.Activities, a);

                a = (Activity) new_entity(Collections.Activities);
                a.setField(Activity.Field.NAME, "Elliptique");
                a.setField(Activity.Field.IS_MODULE, true);
                a.setField(Activity.Field.ICON, "data:image/png;base64,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");
                Database.update_entity(Collections.Activities, a);

                a = (Activity) new_entity(Collections.Activities);
                a.setField(Activity.Field.NAME, "Pause");
                a.setField(Activity.Field.IS_MODULE, false);
                a.setField(Activity.Field.ICON, "data:image/png;base64,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");
                Database.update_entity(Collections.Activities, a);

            } catch (Exception e) {
                LogManager.write(e);
            }
        }

        if (collections.get(Collections.ModuleStates).countDocuments() == 0) {
            try {
                ModuleState m = (ModuleState) new_entity(Collections.ModuleStates);
                m.setField(ModuleState.Field.CODE, 0);
                m.setField(ModuleState.Field.TEXT_FR, "En cours d'envoi");
                m.setField(ModuleState.Field.TEXT_EN, "Shipping");
                Database.update_entity(Collections.ModuleStates, m);

                m = (ModuleState) new_entity(Collections.ModuleStates);
                m.setField(ModuleState.Field.CODE, 1);
                m.setField(ModuleState.Field.TEXT_FR, "Livr\u00e9");
                m.setField(ModuleState.Field.TEXT_EN, "Delivered");
                Database.update_entity(Collections.ModuleStates, m);

                m = (ModuleState) new_entity(Collections.ModuleStates);
                m.setField(ModuleState.Field.CODE, 2);
                m.setField(ModuleState.Field.TEXT_FR, "Fonctionnel");
                m.setField(ModuleState.Field.TEXT_EN, "Fonctionnal");
                Database.update_entity(Collections.ModuleStates, m);

                m = (ModuleState) new_entity(Collections.ModuleStates);
                m.setField(ModuleState.Field.CODE, 3);
                m.setField(ModuleState.Field.TEXT_FR, "En cours d'utilisation");
                m.setField(ModuleState.Field.TEXT_EN, "In use");
                Database.update_entity(Collections.ModuleStates, m);

                m = (ModuleState) new_entity(Collections.ModuleStates);
                m.setField(ModuleState.Field.CODE, 4);
                m.setField(ModuleState.Field.TEXT_FR, "Non fonctionnel");
                m.setField(ModuleState.Field.TEXT_EN, "Non functionnal");
                Database.update_entity(Collections.ModuleStates, m);
            } catch (Exception e) {
                LogManager.write(e);
            }
        }

        if (collections.get(Collections.Feedback_States).countDocuments() == 0) {
            try {
                Feedback_State f = (Feedback_State) new_entity(Collections.Feedback_States);
                f.setField(Feedback_State.Field.CODE, 1);
                f.setField(Feedback_State.Field.TEXT_FR, "En attente");
                f.setField(Feedback_State.Field.TEXT_EN, "Pending");
                Database.update_entity(Collections.Feedback_States, f);

                f = (Feedback_State) new_entity(Collections.Feedback_States);
                f.setField(Feedback_State.Field.CODE, 2);
                f.setField(Feedback_State.Field.TEXT_FR, "En cours");
                f.setField(Feedback_State.Field.TEXT_EN, "In progress");
                Database.update_entity(Collections.Feedback_States, f);

                f = (Feedback_State) new_entity(Collections.Feedback_States);
                f.setField(Feedback_State.Field.CODE, 3);
                f.setField(Feedback_State.Field.TEXT_FR, "R\u00e9solue");
                f.setField(Feedback_State.Field.TEXT_EN, "Solved");
                Database.update_entity(Collections.Feedback_States, f);
            } catch (Exception e) {
                LogManager.write(e);
            }
        }

        if (collections.get(Collections.Administrators).countDocuments() == 0) {
            try {
                Long time = System.currentTimeMillis();

                Administrator admin = (Administrator) new_entity(Collections.Administrators);
                admin.setField(Administrator.Field.FIRSTNAME, "Julien");
                admin.setField(Administrator.Field.LASTNAME, "LONGAYROU");
                admin.setField(Administrator.Field.EMAIL, "julien.longayrou@cegetel.net");
                admin.setField(Administrator.Field.PHONE, "0612345678");
                admin.setField(Administrator.Field.PASSWORD_HASH, new PasswordAuthentication().hash("Totototo-13".toCharArray()));
                admin.setField(Administrator.Field.TOKEN, new Token("julien.longayrou@cegetel.net", "Totototo-13").generate());
                admin.setField(Administrator.Field.CREATION_DATE, time);
                admin.setField(Administrator.Field.UPDATE_DATE, time);
                Database.update_entity(Collections.Administrators, admin);

            } catch (Exception e) {
                LogManager.write(e);
            }
        }
    }
}
