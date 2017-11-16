package model.entities;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import model.Database;
import org.bson.Document;

import javax.print.Doc;

public class _IDS_ extends Document {
    public static class Fields {
        public static String last_Conversation_id = "last_conversation_id";
        public static String last_ElectricProduction_id = "last_ElectricProduction_id";
        public static String last_Event_id = "last_Event_id";
        public static String last_Fitness_Center_id = "last_Fitness_Center_id";
        public static String last_Fitness_Center_Manager_id = "last_Fitness_Center_Manager_id";
        public static String last_Module_id = "last_Module_id";
        public static String last_Picture_id = "last_Picture_id";
        public static String last_User_id = User.Fields.user_id;
    }

    public _IDS_() {
        this.put(Fields.last_Conversation_id, "0");
        this.put(Fields.last_ElectricProduction_id, "0");
        this.put(Fields.last_Event_id, "0");
        this.put(Fields.last_Fitness_Center_id, "0");
        this.put(Fields.last_Fitness_Center_Manager_id, "0");
        this.put(Fields.last_Module_id, "0");
        this.put(Fields.last_Picture_id, "0");
        this.put(Fields.last_User_id, "0");
    }

    public _IDS_(Document doc) {super(doc);}
}
