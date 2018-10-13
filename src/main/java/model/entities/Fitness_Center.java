package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Fitness_Center extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        NAME("name", String.class),
        SIRET("siret", String.class),
        DESCRIPTION("description", String.class),
        ADDRESS("address", String.class),
        ADDRESS_SECOND("address_second", String.class),
        ZIP_CODE("zip_code", String.class),
        CITY("city", String.class),
        PHONE("phone_number", String.class),
        PICTURE_ID("picture_id", ObjectId.class),
        API_KEY("apiKey", String.class),
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

    public Fitness_Center() {
        super();
        setField(Field.ID, new ObjectId());
        setField(Field.API_KEY, "");
    }

    public Fitness_Center(Document doc) {super(doc);}
}
