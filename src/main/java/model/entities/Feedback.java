package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Feedback extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        TITLE("title", String.class),
        DESCRIPTION("description", String.class),
        FEEDBACK_STATE_ID("feedback_state_id", ObjectId.class),
        FITNESS_MANAGER_ID("fitness_manager_id", ObjectId.class),
        CREATION_DATE("creation_date", Long.class),
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

    public Feedback() {
        super();
    }

    public Feedback(Document doc) {super(doc);}
}
