package model;

import org.bson.Document;

/**
 * Created by hadrien on 14/03/2017.
 */
public class Database {

    public static String ip = "localhost";
    public static int port = 27017;
    public static String databaseName = "centralefitness";

    public static class Collections {

        public static class Users {
            public static String key = "users";
            public static class Field {
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
            }
        }
    }

    public static class User extends Database {

        public User() {
            super();
            this.doc.put(Collections.Users.Field.login, null);
            this.doc.put(Collections.Users.Field.passwordHash, null);
            this.doc.put(Collections.Users.Field.token, null);
            this.doc.put(Collections.Users.Field.firstName, null);
            this.doc.put(Collections.Users.Field.lastName, null);
            this.doc.put(Collections.Users.Field.phone, null);
            this.doc.put(Collections.Users.Field.email, null);
            this.doc.put(Collections.Users.Field.wattProduction_total, 0);
            this.doc.put(Collections.Users.Field.wattProduction_year, 0);
            this.doc.put(Collections.Users.Field.wattProduction_month, 0);
            this.doc.put(Collections.Users.Field.wattProduction_week, 0);
            this.doc.put(Collections.Users.Field.wattProduction_day, 0);
        }

        public User(Document doc) {
            super(doc);
        }

        public String getLogin() {
            return (String) (this.doc.containsKey(Collections.Users.Field.login) ? this.doc.get(Collections.Users.Field.login) : null);
        }

        public String getPasswordHash() {
            return (String) (this.doc.containsKey(Collections.Users.Field.passwordHash) ? this.doc.get(Collections.Users.Field.passwordHash) : null);
        }

        public String getToken() {
            return (String) (this.doc.containsKey(Collections.Users.Field.token) ? this.doc.get(Collections.Users.Field.token) : null);
        }

        public String getFirstName() {
            return (String) (this.doc.containsKey(Collections.Users.Field.firstName) ? this.doc.get(Collections.Users.Field.firstName) : null);
        }

        public String getLastName() {
            return (String) (this.doc.containsKey(Collections.Users.Field.lastName) ? this.doc.get(Collections.Users.Field.lastName) : null);
        }

        public String getPhoneNumber() {
            return (String) (this.doc.containsKey(Collections.Users.Field.phone) ? this.doc.get(Collections.Users.Field.phone) : null);
        }

        public String getEmailAddress() {
            return (String) (this.doc.containsKey(Collections.Users.Field.email) ? this.doc.get(Collections.Users.Field.email) : null);
        }

        public double getWattProductionTotal() {
            return (double) (this.doc.containsKey(Collections.Users.Field.wattProduction_total) ? this.doc.get(Collections.Users.Field.wattProduction_total) : 0);
        }

        public double getWattProductionYear() {
            return (double) (this.doc.containsKey(Collections.Users.Field.wattProduction_year) ? this.doc.get(Collections.Users.Field.wattProduction_year) : 0);
        }

        public double getWattProductionMonth() {
            return (double) (this.doc.containsKey(Collections.Users.Field.wattProduction_month) ? this.doc.get(Collections.Users.Field.wattProduction_month) : 0);
        }

        public double getWattProductionWeek() {
            return (double) (this.doc.containsKey(Collections.Users.Field.wattProduction_week) ? this.doc.get(Collections.Users.Field.wattProduction_week) : 0);
        }

        public double getWattProductionDay() {
            return (double) (this.doc.containsKey(Collections.Users.Field.wattProduction_day) ? this.doc.get(Collections.Users.Field.wattProduction_day) : 0);
        }

        public void setLogin(String login) {
            this.doc.put(Collections.Users.Field.login, login);
        }

        public void setPasswordHash(String passwordHash) {
            this.doc.put(Collections.Users.Field.passwordHash, passwordHash);
        }

        public void setToken(String token) {
            this.doc.put(Collections.Users.Field.token, token);
        }

        public void setFirstName(String firstName) {
            this.doc.put(Collections.Users.Field.firstName, firstName);
        }

        public void setLastName(String lastName) {
            this.doc.put(Collections.Users.Field.lastName, lastName);
        }

        public void setPhoneNumber(String phoneNumber) {
            this.doc.put(Collections.Users.Field.phone, phoneNumber);
        }

        public void setEmailAddress(String mailAddress) {
            this.doc.put(Collections.Users.Field.email, mailAddress);
        }
    }

    protected Document doc;
    public Database() { this.doc = new Document();}
    public Database(Document doc) {this.doc = doc;}
    public Document getDoc() {return this.doc;}
}
