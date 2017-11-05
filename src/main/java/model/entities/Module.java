package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

public class Module extends Database.DataDocument {
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
        this.doc.put(Fields.wattProduction_instant, 0.0);
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