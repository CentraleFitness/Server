package model;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import protocol.Protocol;

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

    public static class User extends DataDocument {
        public static class Fields {
            public static String login = "login";
            public static String passwordHash = "passwordHash";
            public static String token = "token";
            public static String firstName = "first name";
            public static String lastName = "last name";
            public static String phone = "phone number";
            public static String email = "email address";
            public static String wattProduction_total = "total watt production";
            public static String wattProduction_year = "year watt production";
            public static String wattProduction_month = "month watt production";
            public static String wattProduction_week = "week watt production";
            public static String wattProduction_day = "day watt production";
            public static String modules = "modules";
        }
        public User() {
            super();
            this.doc.put(Fields.login, null);
            this.doc.put(Fields.passwordHash, null);
            this.doc.put(Fields.token, null);
            this.doc.put(Fields.firstName, null);
            this.doc.put(Fields.lastName, null);
            this.doc.put(Fields.phone, null);
            this.doc.put(Fields.email, null);
            this.doc.put(Fields.wattProduction_total, 0.0);
            this.doc.put(Fields.wattProduction_year, 0.0);
            this.doc.put(Fields.wattProduction_month, 0.0);
            this.doc.put(Fields.wattProduction_week, 0.0);
            this.doc.put(Fields.wattProduction_day, 0.0);
            this.doc.put(Fields.modules, new HashMap<String, ObjectId>()); // module name, module dbID
        }

        public User(Document doc) {
            super(doc);
        }

        public String getLogin() {
            return (String) (this.doc.containsKey(Fields.login) ? this.doc.get(Fields.login) : null);
        }

        public String getPasswordHash() {
            return (String) (this.doc.containsKey(Fields.passwordHash) ? this.doc.get(Fields.passwordHash) : null);
        }

        public String getToken() {
            return (String) (this.doc.containsKey(Fields.token) ? this.doc.get(Fields.token) : null);
        }

        public String getFirstName() {
            return (String) (this.doc.containsKey(Fields.firstName) ? this.doc.get(Fields.firstName) : null);
        }

        public String getLastName() {
            return (String) (this.doc.containsKey(Fields.lastName) ? this.doc.get(Fields.lastName) : null);
        }

        public String getPhoneNumber() {
            return (String) (this.doc.containsKey(Fields.phone) ? this.doc.get(Fields.phone) : null);
        }

        public String getEmailAddress() {
            return (String) (this.doc.containsKey(Fields.email) ? this.doc.get(Fields.email) : null);
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
        public Map<String, ObjectId> getModules() {
            return (Map<String, ObjectId>) (this.doc.containsKey(Fields.modules) ? this.doc.get(Fields.modules) : null);
        }

        public void setLogin(String login) {
            this.doc.put(Fields.login, login);
        }

        public void setPasswordHash(String passwordHash) {
            this.doc.put(Fields.passwordHash, passwordHash);
        }

        public void setToken(String token) {
            this.doc.put(Fields.token, token);
        }

        public void setFirstName(String firstName) {
            this.doc.put(Fields.firstName, firstName);
        }

        public void setLastName(String lastName) {
            this.doc.put(Fields.lastName, lastName);
        }

        public void setPhoneNumber(String phoneNumber) {
            this.doc.put(Fields.phone, phoneNumber);
        }

        public void setEmailAddress(String mailAddress) {
            this.doc.put(Fields.email, mailAddress);
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

    public static class Module extends DataDocument {
        public static class Fields {
            public static String moduleName = "moduleName";
            public static String currentUser = "current user";
            public static String machineType = "type";
            public static String wattProduction_total = "total watt production";
            public static String wattProduction_year = "year watt production";
            public static String wattProduction_month = "month watt production";
            public static String wattProduction_week = "week watt production";
            public static String wattProduction_day = "day watt production";
            public static String wattProduction_instant = "instant watt production";
            public static String users = "users";
        }

        public Module() {
            super();
            this.doc.put(Fields.moduleName, null);
            this.doc.put(Fields.machineType, null);
            this.doc.put(Fields.currentUser, null);
            this.doc.put(Fields.wattProduction_total, 0.0);
            this.doc.put(Fields.wattProduction_year, 0.0);
            this.doc.put(Fields.wattProduction_month, 0.0);
            this.doc.put(Fields.wattProduction_week, 0.0);
            this.doc.put(Fields.wattProduction_day, 0.0);
            this.doc.put(Fields.users, new HashMap<String, ObjectId>()); //user login; user dbID;
        }

        public Module(Document doc) {
            super(doc);
        }

        public String getName() {
            return (String) (this.doc.containsKey(Fields.moduleName) ? this.doc.get(Fields.moduleName) : null);
        }

        public String getCurrentUser() {
            return (String) (this.doc.containsKey(Fields.currentUser) ? this.doc.get(Fields.currentUser) : null);
        }

        public String getMachineType() {
            return (String) (this.doc.containsKey(Fields.machineType) ? this.doc.get(Fields.machineType) : null);
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

        public Map<String, ObjectId> getUsers() {
            return (Map<String, ObjectId>) (this.doc.containsKey(Fields.users) ? this.doc.get(Fields.users) : null);
        }

        public double getWattProductionInstant() {
            return (double) (this.doc.containsKey(Fields.wattProduction_instant) ? this.doc.get(Fields.wattProduction_instant) : 0);
        }

        public void setModuleName(String moduleName) {
            this.doc.put(Fields.moduleName, moduleName);
        }

        public void setCurrentUser(String currentUser) {
            this.doc.put(Fields.currentUser, currentUser);
        }

        public void setMachineType(String machineType) {
            this.doc.put(Fields.machineType, machineType);
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

        public void setWattProduction_instant(double watt) {
            this.doc.put(Fields.wattProduction_instant, watt);
        }
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
