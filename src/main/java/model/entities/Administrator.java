package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Administrator extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        PASSWORD_HASH("password_hash", String.class),
        TOKEN("token", String.class),
        FIRSTNAME("first_name", String.class),
        LASTNAME("last_name", String.class),
        EMAIL("email_address", String.class),
        PHONE("phone_number", String.class),
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

    public Administrator() {
        super();
    }

    public Administrator(Document doc) {super(doc);}
}
