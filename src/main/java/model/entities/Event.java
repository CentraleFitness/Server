package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Event extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        TITLE("title", String.class),
        DESCRIPTION("description", String.class),
        START_DATE("start_date", Long.class),
        END_DATE("end_date", Long.class),
        FITNESS_CENTER_ID("fitness_center_id", ObjectId.class),
        UPDATE_DATE("update_date", Long.class),
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
