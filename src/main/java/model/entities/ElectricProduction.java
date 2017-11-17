package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.math.BigInteger;

public class ElectricProduction extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ELECTRIC_PRODUCTION_ID("electric_production_id", String.class),
        MODULE_ID("module_id", String.class),
        PRODUCTION_TOTAL("production_total", BigInteger.class),
        PRODUCTION_YEAR("production_year", BigInteger.class),
        PRODUCTION_MONTH("production_month", BigInteger.class),
        PRODUCTION_DAY("production_day", BigInteger.class),
        PRODUCTION_INSTANT("production_instant", BigInteger.class),
        USER_ID("user_id", String.class),
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

    public ElectricProduction() {
        super();
    }

    public ElectricProduction(Document doc) {
        super(doc);
    }

}
