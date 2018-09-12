package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public class CustomProgram extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        FITNESS_CENTER_ID("fitness_center_id", ObjectId.class),
        CREATOR_ID("fitness_center_id", ObjectId.class),
        NAME("name", String.class),
        PICTURE_ID("picture_id", ObjectId.class),
        PICTURE("picture", String.class),
        NB_ACTIVITIES("nb_activities", Integer.class),
        TOTAL_TIME("total_time", Integer.class),
        AVAILABLE("available", Boolean.class),
        ACTIVITIES("activities", ArrayList.class),
        UPDATE_DATE("update_date", Long.class),
        CREATION_DATE("creation_date", Long.class),
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

    public CustomProgram() {
        super();
        setField(Field.ID, new ObjectId());
    }

    public CustomProgram(Document doc) {super(doc);}
}
