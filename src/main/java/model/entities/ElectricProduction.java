package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class ElectricProduction extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        MODULE_ID("module_id", String.class),
        PRODUCTION_TOTAL("production_total", Double.class),
        PRODUCTION_YEAR("production_year", Double.class),
        PRODUCTION_MONTH("production_month", Double.class),
        PRODUCTION_DAY("production_day", Double.class),
        PRODUCTION_INSTANT("production_instant", Double.class),
        USER_ID("user_id", String.class),
        LAST_UPDATE("last_update", LocalDateTime.class),
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
        for (User.Field field : User.Field.values())
            try {
                setField(field, field.get_class().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public ElectricProduction(Document doc) {
        super(doc);
    }

    public void updatePoduction() {

    }

    public void addProduction(Object production) {
        LocalDateTime now = LocalDateTime.now();
    }

}
