package model.entities;

import model.Database;
import org.bson.Document;

public class Conversation extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        CONVERSATION_ID("conversation_id", String.class),
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
    }

    public Conversation(Document doc) {super(doc);}
}
