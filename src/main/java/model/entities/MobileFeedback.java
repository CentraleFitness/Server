package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

public class MobileFeedback extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        EMAIL("email", String.class),
        CONTENT("content", String.class),
        DATE("date", String.class),
        VERSION("version", String.class),
        V("__v", Integer.class)
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

    public MobileFeedback() {
        super();
    }

    public MobileFeedback(Document doc) {super(doc);}
}
