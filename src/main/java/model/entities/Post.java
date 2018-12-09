package model.entities;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import model.Database;
import model.Database.Entity;

public class Post extends Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        FITNESS_CENTERT_ID("fitness_center_id", ObjectId.class),
        POSTERID("posterId", ObjectId.class),
        POSTERNAME("posterName", String.class),
        IS_CENTER("is_center", Boolean.class),
        IS_COMMENT("is_comment", Boolean.class),
        TYPE("type", String.class),
        DATE("date", Long.class),

        CONTENT("content", String.class),

        TITLE("title", String.class),

        PICTURE("picture", String.class),
        PICTURE_ID("picture_id", ObjectId.class),

        EVENT_ID("event_id", ObjectId.class),
        START_DATE("start_date", Long.class),
        END_DATE("end_date", Long.class),

        LIKED_BY_CLUB("likedByClub", Boolean.class),
        LIKES("likes", List.class),
        COMMENTS("comments", List.class),
        
        IS_REPORTED("is_reported", List.class),
        REPORTED_BY_CLUB("reported_by_club", Boolean.class),

        IS_DELETED("is_deleted", Boolean.class),
        
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

    public Post() {
        super();
        setField(Field.ID, new ObjectId());
        setField(Field.LIKES, new ArrayList<>());
    }

    public Post(Document doc) {super(doc);}
}
