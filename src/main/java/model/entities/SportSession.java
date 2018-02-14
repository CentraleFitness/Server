package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class SportSession extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        MODULE_ID("module_id", ObjectId.class),
        USER_ID("user_id", ObjectId.class),
        EXPIRATION("expiration", Long.class),
        PRODUCTION("production", ArrayList.class),
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
        setField(Field.ID, new ObjectId());
        setField(Field.MODULE_ID, null);
        setField(Field.USER_ID, null);
        setField(Field.EXPIRATION, 0);
        setField(Field.PRODUCTION, new ArrayList());
    }

    public SportSession(Document doc) {
        super(doc);
    }

}
