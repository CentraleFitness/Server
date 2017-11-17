package model.entities;

import model.Database;
import org.bson.Document;
import org.omg.CORBA.Object;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeSet;

import static model.Database.*;

public class User extends Entity {
    public enum Field implements Entity_Field {
        USER_ID("user_id", String.class),
        FITNESS_CENTER_ID("fitness_center_id", String.class),
        LOGIN("login", String.class),
        PASSWORD_HASH("passwordHash", String.class),
        TOKEN("token", String.class),
        FIRSTNAME("first_name", String.class),
        LASTNAME("last_name", String.class),
        PHONE("phone_number", String.class),
        EMAIL("email_address", String.class),
        PICTURE_ID("picture_id", String.class),
        WATT_PRODUCTION_TOTAL("watt_production_total", BigInteger.class),
        WATT_PRODUCTION_YEAR("watt_production_year", BigInteger.class),
        WATT_PRODUCTION_MONTH("watt_production_month", BigInteger.class),
        WATT_PRODUCTION_WEEK("watt_production_week", BigInteger.class),
        WATT_PRODUCTION_DAY("watt_production_day", BigInteger.class),
        WATT_PRODUCTION_CURRENT("watt_production_current", BigInteger.class),
        CURRENT_MODULE_ID("module_id", String.class),
        FRIENDS("friends", Map.class),
        CONVERSATIONS("conversations", Map.class),
        BLOCKED_USERS("blocked_users", Map.class),
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
    }
    public User(Document doc) {
        super(doc);
    }
}