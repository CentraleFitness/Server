package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Module extends Database.Entity {
    public enum Field implements Database.Entity_Field {
        ID("_id", ObjectId.class),
        UUID("UUID", String.class),
        SESSION_ID("session_id", String.class),
        FITNESS_CENTER_ID("fitness_center_id", ObjectId.class),
        MACHINE_TYPE("machine_type", String.class),
        NEED_NEW_SESSION_ID("need_new_session_id", Boolean.class),
        MODULE_STATE_ID("module_state_id", ObjectId.class),
        MODULE_STATE_CODE("module_state_code", Integer.class),
        CREATION_DATE("creation_date", Long.class),
        UPDATE_DATE("update_date", Long.class),
        CREATOR_ADMIN_ID("creator_admin_id", ObjectId.class),
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

    public Module() {
        super();
        setField(Field.ID, new ObjectId());
        setField(Field.UUID, "");
        setField(Field.SESSION_ID, "");
        setField(Field.MACHINE_TYPE, "");
        setField(Field.NEED_NEW_SESSION_ID, false);
    }

    public Module(Document doc) {
        super(doc);
    }

}