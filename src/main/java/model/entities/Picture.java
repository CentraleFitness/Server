package model.entities;

import org.bson.Document;
import protocol.Protocol;

public class Picture extends Document {
    public static class Fields {
        public static String picture_id = "picture_id";
        public static String picture_base64 = "picture_base64";
    }

    public Picture() {
        super();
        this.put(Fields.picture_id, null);
        this.put(Fields.picture_base64, null);
    }

    public Picture(Document doc) {super(doc);}
}
