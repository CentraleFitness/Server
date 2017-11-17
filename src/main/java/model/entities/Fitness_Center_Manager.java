package model.entities;

import model.Database;
import org.bson.Document;

public class Fitness_Center_Manager extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        FITNESS_CENTER_MANAGER_ID("fitness_center_manager_id", String.class),
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
