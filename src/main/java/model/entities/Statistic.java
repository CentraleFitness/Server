package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Statistic extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        FITNESS_CENTER_ID("fitness_center_id", ObjectId.class),
        PRODUCTION_DAY("production_day", Integer.class),
        PRODUCTION_MONTH("production_month", Integer.class),
        FREQUENTATION_DAY("frequentation_day", Integer.class),
        FREQUENTATION_MONTH("frequentation_month", Integer.class),

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

    public Statistic() {
        super();
        setField(Field.ID, new ObjectId());

    }

    public Statistic(Document doc) {
        super(doc);
    }
}
