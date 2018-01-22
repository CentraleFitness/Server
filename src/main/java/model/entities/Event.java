package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Event extends Database.Entity {
    public enum Field implements Database.Entity_Field {
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

    public Event() {
        super();
    }

    public Event(Document doc) {super(doc);}
}
