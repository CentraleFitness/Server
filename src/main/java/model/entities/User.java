package model.entities;

import java.util.HashMap;
import java.util.HashSet;

import org.bson.Document;
import org.bson.types.ObjectId;

import model.Database.Entity;
import model.Database.Entity_Field;

public class User extends Entity {
    public enum Field implements Entity_Field {
        ID("_id", ObjectId.class),
        FITNESS_CENTER_ID("fitness_center_id", ObjectId.class),
        CREATION_DATE("creation_date", Long.class),
        LOGIN("login", String.class),
        PASSWORD_HASH("passwordHash", String.class),
        TOKEN("token", String.class),
        FIRSTNAME("first_name", String.class),
        LASTNAME("last_name", String.class),
        PHONE("phone_number", String.class),
        EMAIL("email_address", String.class),
        PICTURE_ID("picture_id", ObjectId.class),
        IS_ACTIVE("is_active", Boolean.class),
        FRIENDS("friends", HashMap.class),
        BLOCKED_USERS("blocked_users", HashMap.class),
        FAVORITES_CUSTOM_PROGRAMS("favorites_custom_programs", HashSet.class),
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

    public User() {
        super();
        setField(Field.ID, new ObjectId());
    }

    public User(Document doc) {
        super(doc);
    }
}