package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Module extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        MODULE_ID("module_id", String.class),
        FITNESS_CENTER_ID("fitness_center_id", String.class),
        MACHINE_TYPE("machine_type", String.class),
        CURRENT_USER_ID("current_user_id", String.class),
        PRODUCTION_TOTAL("production_total", BigInteger.class),
        PRODUCTION_YEAR("production_year", BigInteger.class),
        PRODUCTION_MONTH("production_month", BigInteger.class),
        PRODUCTION_DAY("production_day", BigInteger.class),
        PRODUCTION_INSTANT("production_instant", BigInteger.class),
        ELECTRIC_PRODUCTION("electric_production", Map.class),
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
    }

    public Module(Document doc) {
        super(doc);
    }

}