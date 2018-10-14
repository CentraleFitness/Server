package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Fitness_Center_Manager extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        FITNESS_CENTER_ID("fitness_center_id", ObjectId.class),
        PASSWORD_HASH("password_hash", String.class),
        TOKEN("token", String.class),
        FIRSTNAME("first_name", String.class),
        LASTNAME("last_name", String.class),
        PICTURE_ID("picture_id", ObjectId.class),
        EMAIL("email_address", String.class),
        PHONE("phone_number", String.class),
        CREATION_DATE("creation_date", Long.class),
        IS_ACTIVE("is_active", Boolean.class),
        LAST_UPDATE_ACTIVITY("last_update_activity", Long.class),
        LAST_UPDATE_ADMIN_ID("last_update_admin_id", ObjectId.class),
        IS_VALIDATED("is_validated", Boolean.class),
        VALIDATION_DATE("validation_date", Long.class),
        VALIDATOR_ADMIN_ID("validator_admin_id", ObjectId.class),
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

    public Fitness_Center_Manager() {
        super();
    }

    public Fitness_Center_Manager(Document doc) {super(doc);}
}
