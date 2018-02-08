package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.math.BigInteger;
import java.util.ArrayList;

public class SportSession extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        MODULE_ID("module_id", String.class),
        USER_ID("user_id", String.class),
        EXPIRATION("expiration", Float.class),
        PRODUCTION("production", Double.class),
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

    public SportSession() {
        super();
        for (User.Field field : User.Field.values())
            try {
                setField(field, field.get_class().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public SportSession(Document doc) {
        super(doc);
    }

}
