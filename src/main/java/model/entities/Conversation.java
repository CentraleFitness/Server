package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.TreeMap;

public class Conversation extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        USERS_ID("users_id", TreeMap.class),
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

    public Conversation() {
        super();
        for (User.Field field : User.Field.values())
            try {
                setField(field, field.get_class().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public Conversation(Document doc) {super(doc);}
}
