package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TimeZone;

public class ElectricProduction extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        MODULE_ID("module_id", String.class),
        PRODUCTION_TOTAL("production_total", Double.class),
        PRODUCTION_YEAR("production_year", Double.class),
        PRODUCTION_MONTH("production_month", Double.class),
        PRODUCTION_DAY("production_day", Double.class),
        USER_ID("user_id", String.class),
        LAST_UPDATE("last_update", Long.class),
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
        setField(Field.ID, new ObjectId());
        setField(Field.MODULE_ID, "");
        setField(Field.USER_ID, "");
        setField(Field.PRODUCTION_TOTAL, 0d);
        setField(Field.PRODUCTION_YEAR, 0d);
        setField(Field.PRODUCTION_MONTH, 0d);
        setField(Field.PRODUCTION_DAY, 0d);
        setField(Field.LAST_UPDATE, Timestamp.valueOf(LocalDateTime.now()).getTime());
    }

    public ElectricProduction(Document doc) {
        super(doc);
    }

    public void updateProduction() {
        LocalDateTime lastUpdate = LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) getField(Field.LAST_UPDATE)), TimeZone.getDefault().toZoneId());
        LocalDateTime now = LocalDateTime.now();
        setField(Field.LAST_UPDATE, Timestamp.valueOf(now).getTime());
        if (lastUpdate.getYear() < now.getYear()) {
            setField(Field.PRODUCTION_YEAR, 0d);
            setField(Field.PRODUCTION_MONTH, 0d);
            setField(Field.PRODUCTION_DAY, 0d);
        } else if (lastUpdate.getMonth().getValue() < now.getMonth().getValue()) {
            setField(Field.PRODUCTION_MONTH, 0d);
            setField(Field.PRODUCTION_DAY, 0d);
        } else if (lastUpdate.getDayOfMonth() < now.getDayOfMonth()) {
            setField(Field.PRODUCTION_DAY, 0d);
        }
    }

    public void addProduction(Object production_list) {
        ArrayList pl = (ArrayList) production_list;
        double production = 0d;
        for (int i = 0, j = pl.size(); i < j; ++i) {
            production += (double)pl.get(i);
        }
        double finalProduction = production;
        compute(Field.PRODUCTION_TOTAL.key, (key, value) -> (double)value + finalProduction);
        compute(Field.PRODUCTION_YEAR.key, (key, value) -> (double)value + finalProduction);
        compute(Field.PRODUCTION_MONTH.key, (key, value) -> (double)value + finalProduction);
        compute(Field.PRODUCTION_DAY.key, (key, value) -> (double)value + finalProduction);
        updateProduction();
    }
}
