package model;

import static com.mongodb.client.model.Filters.eq;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import model.better.wip.collection.Collection;
import model.entities.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import Tools.LogManager;

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
        Users("users", model.entities.User.class, User.Field.ID.get_key()),
        Modules("modules", model.entities.Module.class, Module.Field.ID.get_key()),
        ElectricProductions("electricproductions", model.entities.ElectricProduction.class, ElectricProduction.Field.ID.get_key()),
        Events("events", model.entities.Event.class, Event.Field.ID.get_key()),
        Conversations("conversations", model.entities.Conversation.class, Conversation.Field.ID.get_key()),
        Pictures("pictures", model.entities.Picture.class, Picture.Field.ID.get_key()),
        Fitness_Centers("fitness_centers", model.entities.Fitness_Center.class, Fitness_Center.Field.ID.get_key()),
        Fitness_Center_Managers("fitness_center_managers", model.entities.Fitness_Center_Manager.class, Fitness_Center_Manager.Field.ID.get_key()),
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

        return collections.get(collection).count();
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
        if (collections.get(Collections.Activities).count() == 0) {
            try {

                Activity a = (Activity) new_entity(Collections.Activities);
                a.setField(Activity.Field.NAME, "Abdominaux");
                a.setField(Activity.Field.ICON, "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAQAAABKfvVzAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QAAKqNIzIAAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfiBAsADAwQ2jLPAAAA/klEQVQ4y93ROy+DYRTA8V8xGKRpbBqxMDAIg9sqDEjsEolNLIjBrIPETCy+AyGxSWySfokm4hYxiEmrJfoYPHmj7fIaOcu5POd/Ls/hv0jbb5InFD3bSw9cCoJgLO0w+Wjl0wEsqwiK2tOlZ5DTp6LflUraLjNqghvjaYGDuHjNRjrgKAJBcCzb8DbkzLnJVqCUICUjMd5pV00QlM03A1knCfJmFbOxyKuK4MNKIwBb3hPoIepTvUY9Ceq2mwGm3P3Y59ZijA+4FgQ7mQisOYxXeDFtDp/2FZSTKXpcGPb47ayrJ1XvdVuyabDlL3MKFjLR6dIRrapq2vP9VfkCYnxm0VqTj+8AAAAldEVYdGRhdGU6Y3JlYXRlADIwMTgtMDQtMTFUMDA6MTI6MTIrMDI6MDA32/g+AAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE4LTA0LTExVDAwOjEyOjEyKzAyOjAwRoZAggAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAAASUVORK5CYII=");
                Database.update_entity(Collections.Activities, a);

                a = (Activity) new_entity(Collections.Activities);
                a.setField(Activity.Field.NAME, "Pompes");
                a.setField(Activity.Field.ICON, "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAQAAABKfvVzAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QAAKqNIzIAAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfiBAsACx/bJeXWAAAA30lEQVQ4y+3RPy+DYRQF8F/9y8tiIaF2i8VQCRKGMlgqkg4Wu4FRRIKVxM7K5iuohcUs6eQDVEzdDBKaa2n7RvPi/QA903NOzj333ufSx/8YzO1ccmrGy1BO+6wnBczlzT8RQnj9q0NiXsmCklG7be0+21pw4NlnOzWEZdtqzk1l2SfVhC911/YsuhJWfx9kRUN4NN1VLoRyhwz0fPKRB0Vn1rx11Ra6u3YeI8qqtkxo2nH3I6aF4bQgsaGqYhzvbh1q9Az5gUhpXQhNNzYlmVsVHRtL6b5L6/JevI9c+AZb6DJXPidACwAAACV0RVh0ZGF0ZTpjcmVhdGUAMjAxOC0wNC0xMVQwMDoxMTozMSswMjowMK8hXt0AAAAldEVYdGRhdGU6bW9kaWZ5ADIwMTgtMDQtMTFUMDA6MTE6MzErMDI6MDDefOZhAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAAABJRU5ErkJggg==");
                Database.update_entity(Collections.Activities, a);

                a = (Activity) new_entity(Collections.Activities);
                a.setField(Activity.Field.NAME, "Elliptique");
                a.setField(Activity.Field.ICON, "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAQAAABKfvVzAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QAAKqNIzIAAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfiBAsACi0K6YUXAAABwklEQVQ4y5XTy0sUYBTG4WeyDLyGKGGKm1ZFSkiQQUKthCChTWBYQeaiENoEUfgHtBIX0YTQIG1chLVylVCaiJsuaiVFaWEwXmAaRkG8fi0SG8lh7Cy/7/e+HM57Dv9ZOVn+C11ywPfd2hX7Igiu71ZwShAEI3+f9mSE81X6KQnGs3tflrLuiTMeuS832yA6NlsJqrJ7l3ixhQd5DrunOTNe7VsaHrwRBHOZ8DqLW2i/UUGwJGjMJOjawmfVOqdPq2KTmnbGq6xsa2fYSfBUx054kVzPvXRRj+VNyQy4a1hROhrRZsKGVWtugDJ9guAdKrwSbJjQJvJH0GtOqyMeSkg5jTIJQfDZbQk9gvNazeiFFklVyPHDTbcsKNAtCJ7pFrx30JRmHJLQwoB2RNyRUijiqyYbggUVrpo05Je4x6DdAEkNOCtYFRe35IGPFl1Bp24R16xadxwNknvFlWNMvyGj6DTthALziKsWxFSqN41ycaKG0pa8UVCXlvyyo2nTHBSl1KyYfOxzwZTYtnRiPqkB+brMKoUaY1IGzQui8rYJ8kStGTcgZUyNzSj2q1crYcSHHXbgmDol3nptOft1/FO/AbEFtSh4LaooAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDE4LTA0LTExVDAwOjEwOjQ1KzAyOjAwvmkY6QAAACV0RVh0ZGF0ZTptb2RpZnkAMjAxOC0wNC0xMVQwMDoxMDo0NSswMjowMM80oFUAAAAZdEVYdFNvZnR3YXJlAHd3dy5pbmtzY2FwZS5vcmeb7jwaAAAAAElFTkSuQmCC");
                Database.update_entity(Collections.Activities, a);

                a = (Activity) new_entity(Collections.Activities);
                a.setField(Activity.Field.NAME, "Pause");
                a.setField(Activity.Field.ICON, "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAMAAAD04JH5AAAAA3NCSVQICAjb4U/gAAAACXBIWXMAABmHAAAZhwF8o32qAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAApdQTFRF////AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoO196gAAANx0Uk5TAAECAwQFBgcICQoLDQ4PEBETFRYXGBkaGxwdHyEiIyQlJicoKSorLC0uLzAxMjM0NTY3ODk6Ozw9Pj9AQUJDREVGR0hJS0xNTk9QUlNUVVZXWFlbXF1fYGFjZGVnaGlqbG1ucHFyc3R2d3h5ent8fX+AgYKDhIaIiYqMjo+QkZKTlJWWl5iZm5ydnp+goaOkpaanqKqrrK2ur7Cxs7S1trm6u72+wMLDxMbIycvMzc7P0NHS1NXW19nb3N3e4OHi4+Tl5ufo6err7O3u7/Dx8vP09fb3+Pn6+/z9/tq0W5sAAAR1SURBVBgZ7cH7Y411HAfw9zmbnRnVjJFImUtICktu5dbkmq2QcilpJVmWmCKX1iSlKOVSCumIrmJKyi2G2czmbGfn8v5j6sfPMztn3+c8z/P5qdcL+E/+nrNMRezoa3lwwbIYUxYaAsdGx+nA+S5wKkhHNsAhfz0d+QIOZUbpSBBOnaAjO+BUMZ2I3Aun/EE6UA7nAmujTNU3AbghK7+wyMyKOKWvMqAr7x9Ke9pBV79LlD5Ph677LlP6NA26Bl2l9EkadD1wjdJHfuh6qIbSFh90Db9OqcIHXSNuUNoEZaPqKa2DsrENlN6GsnEhSmVQNrGR0kooK2iitALKpoQpLYOy6c2UXoGyWRFKS6DsqSiF+AtQNidKIb4AyubFKMSehbL5cQqxuVC2KE4hOhvKFlOKFEFZMaXITChbSql5GpQtpxR+AspKKTU9DnOZBaWbTVQs7IaEVlEKTYC5wZU0FT/SGa1bQ+nmYzDXvZY2HMlAa9ZSahgDG76kLevRivWUbjwCG9IaaMsvuIXvXUp1+bDjbtpzAS35NlOqHQpbMsK0pQ4t+D+gVDMENh2mLWWw8m+jVD0Ydg0M04bq22GRtp3SlYGwb2o1jUWfhEX6TkpV/ZGK3Jc/CyYSohSZBYt2uyhd7Au3lVBqngGLjL2ULvSG20ophafCIrCP0rl74LbVlJomwyJzP6UzPeG2NZQaJ8Gi/UFKf/eA29ZSCo2HRdYhSqE5Y8yM6gZDGyjdfBQWHYJM1fkiGPCVU6ofDavddKAMbfK9T+nGCFjl04nYSLTBv5VS3XC0EKQjO5Cc/2NKtUPR0lU6cgpJpW2ndG0IbnGSjpxAMuk7KVXfj1t9R0c+RBLtdlG6MgCtGBGmA+EBSCxjL6Wq/mjVfKYu+jwSC+yjdLEPEijYWsWUXP92GBLL3E/pQm8k5uuSm4LOPiTR/iClc72gK+sQpTM9oatDkNJfPaCr4/eU/uwOXbcdpXSqG3Td8SOl33OhK/tnSpWdoavTMUrHc6Ar5zilXztBV5dKSj9lQ1fXPyjVFE5q3cS+fnjhzlM0VV+aDtf5j9GGAz647RnasgAuyzhPW36AywK0pzEdLqulLTVwWyVtOQi3jYvShvhouG4JzcVL4YEJ5edo5PLXI+GRTBMB/M9QTm4y2fBWv33XmNzxVXnwTmET21bdE165q44mTnSAR8poZgE8coBmVsIjh2lmMzyyjmaK4ZFBzTRxNgCvlNDEbHhnymm26XV4KmfkmBbeocWrUDajmVIxlBVGKL0IZU9HKcQXQdncGIX4fCh7LkYhNg/KFsYpRGdD2WJKkSIoe4lSZCaULaXUPA3KSig1TYayNyg1ToKy1ZRC46HsLUoNY6FsHaX6UVC2iVLdw9Dlq6B0fRh0+bdQqnkQuvzbKFUPhq607bQoK2phRh8/PJS+k22qmg7vvEkTxfBK15s00ZQHj5TTTAk8sodmdsMj79HMVnhkOc0shEc6nqSJ01nwSq8atu3SUHinR3EwymQiv23sBEf+BXk9huPZRW6OAAAAAElFTkSuQmCC");
                Database.update_entity(Collections.Activities, a);

            } catch (Exception e) {
                LogManager.write(e);
            }
        }

        if (collections.get(Collections.ModuleStates).count() == 0) {
            try {
                ModuleState m = (ModuleState) new_entity(Collections.ModuleStates);
                m.setField(ModuleState.Field.TEXT_FR, "En cours d’envoi");
                m.setField(ModuleState.Field.TEXT_EN, "Shipping");
                Database.update_entity(Collections.ModuleStates, m);

                m = (ModuleState) new_entity(Collections.ModuleStates);
                m.setField(ModuleState.Field.TEXT_FR, "Livré");
                m.setField(ModuleState.Field.TEXT_EN, "Delivered");
                Database.update_entity(Collections.ModuleStates, m);

                m = (ModuleState) new_entity(Collections.ModuleStates);
                m.setField(ModuleState.Field.TEXT_FR, "Fonctionnel");
                m.setField(ModuleState.Field.TEXT_EN, "Fonctionnal");
                Database.update_entity(Collections.ModuleStates, m);

                m = (ModuleState) new_entity(Collections.ModuleStates);
                m.setField(ModuleState.Field.TEXT_FR, "En cours d'utilisation");
                m.setField(ModuleState.Field.TEXT_EN, "In use");
                Database.update_entity(Collections.ModuleStates, m);

                m = (ModuleState) new_entity(Collections.ModuleStates);
                m.setField(ModuleState.Field.TEXT_FR, "Non fonctionnel");
                m.setField(ModuleState.Field.TEXT_EN, "Non functionnal");
                Database.update_entity(Collections.ModuleStates, m);
            } catch (Exception e) {
                LogManager.write(e);
            }
        }

        if (collections.get(Collections.Feedback_States).count() == 0) {
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
                f.setField(Feedback_State.Field.TEXT_FR, "Résolue");
                f.setField(Feedback_State.Field.TEXT_EN, "Solved");
                Database.update_entity(Collections.Feedback_States, f);
            } catch (Exception e) {
                LogManager.write(e);
            }
        }
    }
}
