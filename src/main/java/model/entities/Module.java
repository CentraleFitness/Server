package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Module extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        UUID("UUID", String.class),
        SESSION_ID("session_id", String.class),
        FITNESS_CENTER_ID("fitness_center_id", ObjectId.class),
        MACHINE_TYPE("machine_type", String.class),
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

    public Module() {
        super();
        for (User.Field field : User.Field.values())
            try {
                setField(field, field.get_class().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public Module(Document doc) {
        super(doc);
    }

}