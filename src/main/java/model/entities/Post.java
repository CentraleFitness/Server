package model.entities;

import org.bson.Document;
import org.bson.types.ObjectId;

import model.Database;
import model.Database.Entity;
import model.entities.Event.Field;

public class Post extends Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        POSTERID("posterId", ObjectId.class),
        TYPE("type", String.class),
        DATE("date", Long.class),
        CONTENT("content", Object.class),
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

    public Post() {
        super();
        setField(Field.ID, new ObjectId());
    }

    public Post(Document doc) {super(doc);}
}
