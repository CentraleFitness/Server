package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public class Activity extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        NAME("name", String.class),
        IS_MODULE("is_module", Boolean.class),
        ICON("icon", String.class),
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

    public Activity() {
        super();
        setField(Field.ID, new ObjectId());
    }

    public Activity(Document doc) {super(doc);}
}
