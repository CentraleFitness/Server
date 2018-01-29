package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Feedback_State extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        CODE("code", Integer.class),
        TEXT_FR("text_fr", String.class),
        TEXT_EN("text_en", String.class),
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

    public Feedback_State() {
        super();
    }

    public Feedback_State(Document doc) {super(doc);}
}
