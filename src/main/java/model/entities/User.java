package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

import static model.Database.*;

public class User extends Entity {
    public enum Field implements Entity_Field {
        ID("_id", ObjectId.class),
        FITNESS_CENTER_ID("fitness_center_id", ObjectId.class),
        LOGIN("login", String.class),
        PASSWORD_HASH("passwordHash", String.class),
        TOKEN("token", String.class),
        FIRSTNAME("first_name", String.class),
        LASTNAME("last_name", String.class),
        PHONE("phone_number", String.class),
        EMAIL("email_address", String.class),
        PICTURE_ID("picture_id", ObjectId.class),
        FRIENDS("friends", HashMap.class),
        BLOCKED_USERS("blocked_users", HashMap.class),
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
        for (Field field : User.Field.values())
            try {
                setField(field, field.get_class().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    public User(Document doc) {
        super(doc);
    }
}