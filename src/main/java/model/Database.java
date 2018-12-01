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
                a.setField(Activity.Field.ICON, "data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTYuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgd2lkdGg9IjUxMnB4IiBoZWlnaHQ9IjUxMnB4IiB2aWV3Qm94PSIwIDAgMTk3LjU5NSAxOTcuNTk0IiBzdHlsZT0iZW5hYmxlLWJhY2tncm91bmQ6bmV3IDAgMCAxOTcuNTk1IDE5Ny41OTQ7IiB4bWw6c3BhY2U9InByZXNlcnZlIj4KPGc+Cgk8Zz4KCQk8ZWxsaXBzZSBjeD0iMTguMzMzIiBjeT0iNTguNjkzIiByeD0iMTguMzMzIiByeT0iMTguMDkxIiBmaWxsPSIjMDAwMDAwIi8+CgkJPHBhdGggZD0iTTE5NC4zOTYsMTM2LjQxNWwtMzguMjE2LTM0Ljg2M2MtMy4yNTktMi45NzItOC4xMDEtMy40MDMtMTEuODMyLTEuMDUzbC0zOC4zNjgsMjQuMTUzICAgIGMtMS43MzUsMS4wOTMtMy45NjYsMC45OTYtNS42MDEtMC4yNDFMNDQuMTAzLDgxLjgyOWMtNi43MjEtNS4wNzgtMTYuMjg2LTMuNzQ4LTIxLjM2NSwyLjk3MSAgICBjLTIuMDc3LDIuNzQ5LTMuMDQ5LDUuOTczLTMuMDUxLDkuMTdjLTAuMDAxLDAuMDEsMC4wMywwLjAxOCwwLjAzLDAuMDI3djU1LjQ5NmMwLDQuMTQzLDMuMzU4LDcuNSw3LjUsNy41SDY5LjYgICAgYzMuNjExLDAsNi41MzctMi45NjgsNi41MzctNi41NzhjMC0zLjYwOS0yLjkyNi02LjU3OC02LjUzNy02LjU3OEgzNS4xNzRjLTEuMjcxLDAtMi4zLTEuMDI5LTIuMy0yLjMwMXYtMzAuMDY4bDUxLjA3NCwzOC42NTggICAgYzIuMDM4LDEuNTM5LDkuOTYyLDQuODgxLDE4LjAxNiwwLjIyOWMxMC4zMzUtNS45NzQsMzcuODE3LTIzLjY2OCw0NC44NTQtMjguMjExYzAuOTgxLTAuNjM0LDIuMjYyLTAuNTI0LDMuMTI1LDAuMjYyICAgIGwzMS4yMzYsMjguNDk4YzQuMDAxLDMuNjUxLDEwLjIwMiwzLjM2MiwxMy44NTQtMC42MzhDMTk4LjY4NCwxNDYuMjY4LDE5OC4zOTgsMTQwLjA2NiwxOTQuMzk2LDEzNi40MTV6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8L2c+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPC9zdmc+Cg==");
                Database.update_entity(Collections.Activities, a);

                a = (Activity) new_entity(Collections.Activities);
                a.setField(Activity.Field.NAME, "Pompes");
                a.setField(Activity.Field.IS_MODULE, false);
                a.setField(Activity.Field.ICON, "data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTYuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgd2lkdGg9IjUxMnB4IiBoZWlnaHQ9IjUxMnB4IiB2aWV3Qm94PSIwIDAgMTc1LjQ5NSAxNzUuNDk1IiBzdHlsZT0iZW5hYmxlLWJhY2tncm91bmQ6bmV3IDAgMCAxNzUuNDk1IDE3NS40OTU7IiB4bWw6c3BhY2U9InByZXNlcnZlIj4KPGc+Cgk8cGF0aCBkPSJNMTM5LjA5OCw4MS45NTJjMi4yNzEsMS40ODUsMy4zMzgsNC4yMzIsMi42NDMsNi44NTljLTAuNywyLjYxMy0yLjk5OCw0LjQ3NC01LjY5Niw0LjYxNWwtMjMuMjY3LDEuMjY1bDMuMDM3LDE1LjIyMyAgIGMwLjY4MSwzLjM3MS0xLjUyMSw2LjY1NC00Ljg5MSw3LjMzM2MtMC40MTUsMC4wOTItMC44MjIsMC4xMi0xLjIzMiwwLjEyYy0yLjkxNywwLTUuNTIxLTIuMDQ3LTYuMTEyLTUuMDIybC0zLjM5My0xNi45NjUgICBsLTIwLjc0OCwxLjExOGwtMzIuNzksMTQuMTI4Yy0wLjg1NiwwLjM3MS0xLjc2NSwwLjUzLTIuNzI0LDAuNTE1bC0zNy45NDQtMS41NmMtMy40NDItMC4xMzgtNi4xMTUtMy4wNTQtNS45NzctNi40ODQgICBjMC4xNDItMy40MzcsMy4zNzQtNS45ODEsNi40OS01Ljk4MWwzNi41MjksMS41MDVsMzIuNTA0LTEzLjk5NWMwLjY3My0wLjI5NiwxLjQwMS0wLjQ1OSwyLjEzNy0wLjQ5M2wyMC4wNi0xLjA4NmwtMy40OTctMTcuNDYxICAgYy0wLjQ5Ni0yLjQ1NCwwLjUzNi00Ljk3NCwyLjYxLTYuMzc1YzIuMDg0LTEuNDAxLDQuNzk1LTEuNDUzLDYuODg5LTAuMDg1TDEzOS4wOTgsODEuOTUyeiBNMTU5LjYyNCw3MC4zMTUgICBjLTguNzU0LDAtMTUuODUyLDcuMDk1LTE1Ljg1MiwxNS44NTdjMCw4Ljc1Myw3LjEwMiwxNS44MzksMTUuODUyLDE1LjgzOXMxNS44NzEtNy4wOTIsMTUuODcxLTE1LjgzOSAgIEMxNzUuNDg3LDc3LjM5OSwxNjguMzc4LDcwLjMxNSwxNTkuNjI0LDcwLjMxNXoiIGZpbGw9IiMwMDAwMDAiLz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8L3N2Zz4K");
                Database.update_entity(Collections.Activities, a);

                a = (Activity) new_entity(Collections.Activities);
                a.setField(Activity.Field.NAME, "Elliptique");
                a.setField(Activity.Field.IS_MODULE, true);
                a.setField(Activity.Field.ICON, "data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgdmlld0JveD0iMCAwIDUxMiA1MTIiIHN0eWxlPSJlbmFibGUtYmFja2dyb3VuZDpuZXcgMCAwIDUxMiA1MTI7IiB4bWw6c3BhY2U9InByZXNlcnZlIiB3aWR0aD0iNTEycHgiIGhlaWdodD0iNTEycHgiPgo8Zz4KCTxnPgoJCTxwYXRoIGQ9Ik0yNzYsMzkyYy01LjUyLDAtMTAsNC40OC0xMCwxMGMwLDUuNTIsNC40OCwxMCwxMCwxMGM1LjUyLDAsMTAtNC40OCwxMC0xMEMyODYsMzk2LjQ4LDI4MS41MiwzOTIsMjc2LDM5MnoiIGZpbGw9IiMwMDAwMDAiLz4KCTwvZz4KPC9nPgo8Zz4KCTxnPgoJCTxwYXRoIGQ9Ik00ODIsNDUyaC0wLjQ0OEM0OTkuOTYyLDQzNy4wODIsNTEyLDQxNS4zMjUsNTEyLDM5MmMwLTMzLjc4Mi0yMS4zNzktNjQuMDg0LTUzLjE5OC03NS40MDIgICAgYy0wLjA0My0wLjAxNS0wLjA4Ni0wLjAzLTAuMTI5LTAuMDQ1bC04NS40NzktMjkuMTAzTDQwNC42MywxMjZINDE2YzI3LjU3LDAsNTAtMjIuNDMsNTAtNTBWMzBjMC0xNi41NDItMTMuNDU4LTMwLTMwLTMwICAgIGMtMTYuNTc1LDAtMzAsMTMuNDI1LTMwLDMwdjM2aC01MGMtMTYuNTQyLDAtMzAsMTMuNDU4LTMwLDMwYzAsMTIuMTc0LDcuNDIxLDIyLjc4LDE3Ljk3NCwyNy40NTNsLTI4LjA3NywxNDQuNDg5bC0xNDIuNDEtNDguNDg2ICAgIGMtOC4zNjgtMi45NzQtMTcuMDQ0LTUuMDYtMjUuODYzLTYuMjUzTDEzMC4xMjcsMTQ2aDMyLjgwM2M5Ljg5OSwwLDE5LjIwOC0zLjg1NiwyNi4yMTEtMTAuODU4ICAgIEMxOTYuMTQ0LDEyOC4xMzksMjAwLDExOC44MywyMDAsMTA4LjkzYzAtMTguMTg4LTEzLjAyNi0zMy41NjUtMzAuOTc2LTM2LjU2M0M0Ny4xMiw1Mi4yNzQsNTMuNTE0LDUyLjYxLDQ2LjYxLDUyLjYxICAgIEMyMC45MDksNTIuNjEsMCw3My41NiwwLDk5LjMxQzAsMTI1LjA1NSwyMC45NDUsMTQ2LDQ2LjY5LDE0NmgyMS4yNDZsMTkuMTMyLDczLjI3MUMzNS4zMzYsMjM3LjMyMSwwLDI4Ni40MzYsMCwzNDIgICAgYzAsNDUuODcsMjQuMDA4LDg2LjgxMiw2MC42OSwxMTBIMzBjLTE2LjU3NCwwLTMwLDEzLjQyNC0zMCwzMGMwLDE2LjU0MiwxMy40NTgsMzAsMzAsMzBoNDUyYzE2LjU3NSwwLDMwLTEzLjQyNSwzMC0zMCAgICBDNTEyLDQ2NS40NTgsNDk4LjU0Miw0NTIsNDgyLDQ1MnogTTM1Niw4Nmg2MGM1LjUyMiwwLDEwLTQuNDc4LDEwLTEwVjMwYzAtNS41MTksNC40NzktMTAsMTAtMTBjNS41MTQsMCwxMCw0LjQ4NiwxMCwxMHY0NiAgICBjMCwxNi41NDItMTMuNDU4LDMwLTMwLDMwYy0xMS44MSwwLTQ3LjE5NSwwLTU5LjU4NSwwYy0wLjMyLTAuMDI2LTAuNjM3LTAuMDM2LTAuOTUzLTAuMDMxQzM1MC4yMTUsMTA1LjY5NCwzNDYsMTAxLjI5NywzNDYsOTYgICAgQzM0Niw5MC40ODYsMzUwLjQ4Niw4NiwzNTYsODZ6IE0zNjMuODUyLDEyNmgyMC40MDNsLTMwLjE2OSwxNTQuOTQ0bC0xOS4wNzktNi40OTZMMzYzLjg1MiwxMjZ6IE00Ni42OSwxMjYgICAgQzMxLjk3MywxMjYsMjAsMTE0LjAyNiwyMCw5OS4zMWMwLTE0LjcyMiwxMS45MzctMjYuNywyNi42MS0yNi43YzQuMDksMC0wLjkwOC0wLjM5MiwxMTkuMTIzLDE5LjQ4MyAgICBDMTc0LDkzLjQ3NSwxODAsMTAwLjU1NSwxODAsMTA4LjkzYzAsNC41NTgtMS43NzYsOC44NDUtNS4wMDEsMTIuMDY5Yy0zLjIyNSwzLjIyNS03LjUxMSw1LjAwMS0xMi4wNjksNS4wMDFINDYuNjl6ICAgICBNMTI2LjY1OCwyMTIuMDU0Yy02LjgyMywwLjE3My0xMy41OTcsMC44ODEtMjAuMjU1LDIuMTA0TDg4LjYwNywxNDZoMjAuODUzTDEyNi42NTgsMjEyLjA1NHogTTIwLDM0MiAgICBjMC00OS44NTMsMzMuNjA1LTkzLjU3NSw4MS43MzMtMTA2LjMyN2MyMC42NDItNS40OTcsNDMuNjY3LTQuOTksNjUuMTEyLDIuNjQ4YzAuMDQ0LDAuMDE2LDAuMDg4LDAuMDMsMC4xMzIsMC4wNDYgICAgbDI4NS4xODYsOTcuMDk3QzQ3NS45OTMsMzQzLjk3Miw0OTIsMzY2LjY4NCw0OTIsMzkyYzAsMzEuMzk1LTMxLjQ0Miw1OC45ODYtNjUuOTcxLDU5bC0zMTAuMjc1LDAuMDkyICAgIEM2MS4xMjIsNDQ0LjAyOSwyMCwzOTcuMjM0LDIwLDM0MnogTTQ4Miw0OTJIMzBjLTUuNTE0LDAtMTAtNC40ODYtMTAtMTBjMC01LjUyMSw0LjQ3OS0xMCwxMC0xMGM4LjM2NCwwLDM2Mi4wNjIsMCw0NTIsMCAgICBjNS41MTQsMCwxMCw0LjQ4NiwxMCwxMEM0OTIsNDg3LjUxOSw0ODcuNTIxLDQ5Miw0ODIsNDkyeiIgZmlsbD0iIzAwMDAwMCIvPgoJPC9nPgo8L2c+CjxnPgoJPGc+CgkJPHBhdGggZD0iTTM3NiwzMzJjLTI3LjU3LDAtNTAsMjIuNDMtNTAsNTBzMjIuNDMsNTAsNTAsNTBzNTAtMjIuNDMsNTAtNTBTNDAzLjU3LDMzMiwzNzYsMzMyeiBNMzc2LDQxMmMtMTYuNTQyLDAtMzAtMTMuNDU4LTMwLTMwICAgIHMxMy40NTgtMzAsMzAtMzBzMzAsMTMuNDU4LDMwLDMwUzM5Mi41NDIsNDEyLDM3Niw0MTJ6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8L2c+CjwvZz4KPGc+Cgk8Zz4KCQk8cGF0aCBkPSJNMjM2LDM5MmgtNDEuODU4bC04LjA3Ni04LjA3NkMxOTUuMTA0LDM3MS44ODIsMjAwLDM1Ny4zMTcsMjAwLDM0MmMwLTM4LjU5OC0zMS40MDItNzAtNzAtNzBjLTM4LjU5OCwwLTcwLDMxLjQwMi03MCw3MCAgICBjMCwzOC41OTgsMzEuNDAyLDcwLDcwLDcwYzE1LjMxNSwwLDI5Ljg3OS00Ljg5NCw0MS45MjMtMTMuOTM0bDExLjAwNiwxMS4wMDZjMS44NzUsMS44NzUsNC40MTksMi45MjksNy4wNzEsMi45MjloNDYgICAgYzUuNTIyLDAsMTAtNC40NzgsMTAtMTBDMjQ2LDM5Ni40NzksMjQxLjUyMiwzOTIsMjM2LDM5MnogTTE3MS43MjIsMzY5LjU4bC0zNC42NTEtMzQuNjUxYy0zLjkwNS0zLjkwNC0xMC4yMzctMy45MDQtMTQuMTQyLDAgICAgYy0zLjkwNSwzLjkwNS0zLjkwNSwxMC4yMzcsMCwxNC4xNDNsMzQuNjUsMzQuNjVDMTQ5LjQ2NSwzODkuMTA2LDEzOS45NTQsMzkyLDEzMCwzOTJjLTI3LjU3LDAtNTAtMjIuNDMtNTAtNTBzMjIuNDMtNTAsNTAtNTAgICAgczUwLDIyLjQzLDUwLDUwQzE4MCwzNTEuOTU1LDE3Ny4xMDUsMzYxLjQ2OCwxNzEuNzIyLDM2OS41OHoiIGZpbGw9IiMwMDAwMDAiLz4KCTwvZz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8L3N2Zz4K");
                Database.update_entity(Collections.Activities, a);

                a = (Activity) new_entity(Collections.Activities);
                a.setField(Activity.Field.NAME, "Pause");
                a.setField(Activity.Field.IS_MODULE, false);
                a.setField(Activity.Field.ICON, "data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTYuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgd2lkdGg9IjUxMnB4IiBoZWlnaHQ9IjUxMnB4IiB2aWV3Qm94PSIwIDAgNjkuNDA2IDY5LjQwNyIgc3R5bGU9ImVuYWJsZS1iYWNrZ3JvdW5kOm5ldyAwIDAgNjkuNDA2IDY5LjQwNzsiIHhtbDpzcGFjZT0icHJlc2VydmUiPgo8Zz4KCTxwYXRoIGQ9Ik0zMi45NDksMzQuMjExSDMuODQxYy0xLjEzOSwwLTIuMTY0LTAuNjg3LTIuNTk4LTEuNzQxYy0wLjQzLTEuMDUzLTAuMTc4LTIuMjUxLDAuNjI5LTMuMDU0TDI2LjEsNS42MDRIMy44NDEgICBjLTEuNTUsMC0yLjgwMi0xLjI2Ny0yLjgwMi0yLjgwMkMxLjA0LDEuMjQ4LDIuMjkyLDAsMy44NDEsMGgyOS4xMDhjMS4xMzcsMCwyLjE2NCwwLjY4OSwyLjU5MywxLjc0MyAgIGMwLjQzMiwxLjA1MywwLjE4LDIuMjUyLTAuNjI3LDMuMDUzTDEwLjY4NywyOC42MWgyMi4yNjNjMS41NDUsMCwyLjgwMiwxLjI2MSwyLjgwMiwyLjgwMiAgIEMzNS43NTEsMzIuOTYzLDM0LjQ5LDM0LjIxMSwzMi45NDksMzQuMjExeiBNNjUuNTYyLDM5Ljk2N0g1MS44NDlsMTUuNjg4LTE1LjQyYzAuODEyLTAuODA3LDEuMDUxLTIsMC42My0zLjA1MyAgIGMtMC40My0xLjA1My0xLjQ2Ni0xLjc0Ny0yLjYwNC0xLjc0N0g0NC45OTVjLTEuNTM4LDAtMi44MDIsMS4yNTUtMi44MDIsMi44MDFjMCwxLjUzNSwxLjI2NCwyLjgwMiwyLjgwMiwyLjgwMmgxMy43MjEgICBMNDMuMDI3LDQwLjc2NmMtMC44MTgsMC44MS0xLjA1OCwxLjk5OC0wLjYyNywzLjA1NGMwLjQzNCwxLjA2MiwxLjQ1NiwxLjc1MSwyLjU5NSwxLjc1MWgyMC41NjZjMS41NDksMCwyLjgwMi0xLjI1OSwyLjgwMi0yLjgwMiAgIEM2OC4zNjMsNDEuMjI1LDY3LjExLDM5Ljk2Nyw2NS41NjIsMzkuOTY3eiBNMzMuOTM1LDYzLjgwNGgtOS41NjVMMzUuOSw1Mi40NzdjMC44MTQtMC43OTksMS4wNi0yLjAwOSwwLjYzLTMuMDYzICAgYy0wLjQzMi0xLjA1Mi0xLjQ1OS0xLjc0MS0yLjU5Ni0xLjc0MUgxNy41MjRjLTEuNTQ3LDAtMi44MDMsMS4yNTgtMi44MDMsMi44MDJjMCwxLjU0MiwxLjI1NiwyLjgwMiwyLjgwMywyLjgwMmg5LjU2NSAgIEwxNS41NTcsNjQuNjAyYy0wLjgxMiwwLjgxLTEuMDYsMS45OTgtMC42MjcsMy4wNTRjMC40MzEsMS4wNTYsMS40NTYsMS43NTEsMi41OTUsMS43NTFoMTYuNDExYzEuNTQ3LDAsMi44MDEtMS4yNTksMi44MDEtMi44MDIgICBTMzUuNDk0LDYzLjgwNCwzMy45MzUsNjMuODA0eiIgZmlsbD0iIzAwMDAwMCIvPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+Cjwvc3ZnPgo=");
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
