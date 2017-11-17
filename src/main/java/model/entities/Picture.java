package model.entities;

import model.Database;
import org.bson.Document;
import protocol.Protocol;

public class Picture extends Document {
    public enum Field implements Database.Entity_Field {
        PICTURE_ID("picture_id", String.class),
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

    public Picture() {
        super();
    }

    public Picture(Document doc) {super(doc);}
}
