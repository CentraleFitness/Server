package model.entities;

import model.Database;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

public class User extends Database.DataDocument {
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