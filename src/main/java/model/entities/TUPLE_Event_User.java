package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

public class TUPLE_Event_User extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        EVENT_ID("event_id", ObjectId.class),
        USER_ID("user_id", ObjectId.class),
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

    public TUPLE_Event_User() {
        super();
    }

    public TUPLE_Event_User(Document doc) {super(doc);}
}
