package model;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hadrien on 14/03/2017.
 */
public class Database {

    public MongoClient client = null;
    public MongoDatabase db = null;
    public MongoCollection users = null;
    public MongoCollection modules = null;
    public MongoCollection electricProductions = null;

    public static String ip = "localhost";
    public static int port = 27017;
    public static String name = "centralefitness";
    public static String idKey = "_id";

    public static class Collections {

        public static class Users {
            public static String key = "users";
        }

        public static class Modules {
            public static String key = "modules";
        }

        public static class ElectricProductions {
            public static String key = "electricproductions";
        }

        public static class Events {
            public static String key = "events";
        }

        public static class Conversations {
            public static String key = "conversations";
        }
    }

    public Database() {
        this.client = new MongoClient(Database.ip, Database.port);
        this.db = this.client.getDatabase(Database.name);
        this.users = this.db.getCollection(Collections.Users.key);
        this.modules = this.db.getCollection(Collections.Modules.key);
        this.electricProductions = this.db.getCollection(Collections.ElectricProductions.key);
}

    public static class DataDocument {
        protected Document doc;
        public DataDocument() { this.doc = new Document();}
        public DataDocument(Document doc) {this.doc = doc;}
        public Document getDoc() {return this.doc;}
        public Document getUpdate() {return new Document("$set", this.doc);}
        public ObjectId getId() {return (ObjectId) this.doc.get("_id");}
    }

    public static class ElectricProduction extends DataDocument {
        public static class Fields {
            public static String userId = "userId";
            public static String moduleId = "moduleId";
            public static String wattProduction_total = "total watt production";
            public static String wattProduction_year = "year watt production";
            public static String wattProduction_month = "month watt production";
            public static String wattProduction_week = "week watt production";
            public static String wattProduction_day = "day watt production";
        }

        public ElectricProduction() {
            super();
            this.doc.put(Fields.userId, null);
            this.doc.put(Fields.moduleId, null);
            this.doc.put(Fields.wattProduction_total, 0.0);
            this.doc.put(Fields.wattProduction_year, 0.0);
            this.doc.put(Fields.wattProduction_month, 0.0);
            this.doc.put(Fields.wattProduction_week, 0.0);
            this.doc.put(Fields.wattProduction_day, 0.0);
        }

        public ElectricProduction(Document doc) {
            super(doc);
        }

        public ObjectId getUserId() {
            return (ObjectId) (this.doc.containsKey(Fields.userId) ? this.doc.get(Fields.userId) : 0);
        }

        public ObjectId getModuleId() {
            return (ObjectId) (this.doc.containsKey(Fields.moduleId) ? this.doc.get(Fields.moduleId) : 0);
        }

        public double getWattProductionTotal() {
            return (double) (this.doc.containsKey(Fields.wattProduction_total) ? this.doc.get(Fields.wattProduction_total) : 0);
        }

        public double getWattProductionYear() {
            return (double) (this.doc.containsKey(Fields.wattProduction_year) ? this.doc.get(Fields.wattProduction_year) : 0);
        }

        public double getWattProductionMonth() {
            return (double) (this.doc.containsKey(Fields.wattProduction_month) ? this.doc.get(Fields.wattProduction_month) : 0);
        }

        public double getWattProductionWeek() {
            return (double) (this.doc.containsKey(Fields.wattProduction_week) ? this.doc.get(Fields.wattProduction_week) : 0);
        }

        public double getWattProductionDay() {
            return (double) (this.doc.containsKey(Fields.wattProduction_day) ? this.doc.get(Fields.wattProduction_day) : 0);
        }

        public void setUserId(ObjectId userId) {
            this.doc.put(Fields.userId, userId);
        }

        public void setModuleId(ObjectId moduleId) {
            this.doc.put(Fields.moduleId, moduleId);
        }

        public void setWattProduction_total(double watt) {
            this.doc.put(Fields.wattProduction_total, watt);
        }

        public void setWattProduction_year(double watt) {
            this.doc.put(Fields.wattProduction_year, watt);
        }

        public void setWattProduction_month(double watt) {
            this.doc.put(Fields.wattProduction_month, watt);
        }

        public void setWattProduction_week(double watt) {
            this.doc.put(Fields.wattProduction_week, watt);
        }

        public void setWattProduction_day(double watt) {
            this.doc.put(Fields.wattProduction_day, watt);
        }
    }
}
