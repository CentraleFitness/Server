package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.LinkedList;
import java.util.List;

public class Challenge extends Database.Entity {

    public enum ChallengeType {
        COLLECTIF("collectif"),
        INDIVIDUAL("individual");

        public final String value;

        ChallengeType(String type) {
            this.value = type;
        }
    }

    public enum Machines {
        BIKE("bike"),
        HELIPTIC("heliptic"),
        TRAKE("trake");

        public final String value;

        Machines(String machine) {
            this.value = machine;
        }
    }



    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        TYPE("type", String.class),
        TITLE("title", String.class),
        OWNER("owner", String.class),
        STEPS("steps", List.class),
        DESC("description", String.class),
        MACHINE("machine", String.class),
        ENDDATE("endDate", String.class),
        POINTSNEEDED("pointsNeeded", Integer.class)
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

    public String toJson(){
        return "";
    }

    public Challenge() {
        super();
        setField(Field.ID, new ObjectId());
    }

    public Challenge(Document doc) {super(doc);}
}
