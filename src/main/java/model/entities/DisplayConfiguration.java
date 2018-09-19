package model.entities;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import model.Database;
import model.Database.Entity;

public class DisplayConfiguration extends Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        FITNESS_CENTER_ID("fitness_center_id", ObjectId.class),
        SHOW_EVENTS("show_events", Boolean.class),
        SELECTED_EVENTS("selected_events", ArrayList.class),
        SHOW_NEWS("show_news", Boolean.class),
        NEWS_TYPE("news_type", String.class),
        SHOW_GLOBAL_PERFORMANCES("show_global_performances", Boolean.class),
        PERFORMANCES_TYPE("performances_type", String.class),
        SHOW_RANKING_DISCIPLINE("show_ranking_discipline", Boolean.class),
        RANKING_DISCIPLINE_TYPE("ranking_discipline_type", String.class),
        SHOW_GLOBAL_RANKING("show_global_ranking", Boolean.class),
        SHOW_NATIONAL_PRODUCTION_RANKING("show_national_production_rank", Boolean.class),

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

    public DisplayConfiguration() {
        super();
        setField(Field.ID, new ObjectId());
    }

    public DisplayConfiguration(Document doc) {super(doc);}
}
