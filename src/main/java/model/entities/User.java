package model.entities;

import model.Database;
import org.bson.Document;
import java.util.TreeSet;

public class User extends Document {
    public static class Fields {
        public static String user_id = "user_id";
        public static String fitness_center_id = "fitness_center_id";
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
        public static String current_module_id = "current_module_id";
        public static String electricProductions = "electricProductions";
        public static String friends = "friends";
        public static String conversations = "conversations";
        public static String blocked_users = "blocked_users";
    }
    public User() {
        super();
        this.put(Fields.user_id, null);
        this.put(Fields.fitness_center_id, null);
        this.put(Fields.login, null);
        this.put(Fields.passwordHash, null);
        this.put(Fields.token, null);
        this.put(Fields.firstName, null);
        this.put(Fields.lastName, null);
        this.put(Fields.phone, null);
        this.put(Fields.email, null);
        this.put(Fields.wattProduction_total, 0.0);
        this.put(Fields.wattProduction_year, 0.0);
        this.put(Fields.wattProduction_month, 0.0);
        this.put(Fields.wattProduction_week, 0.0);
        this.put(Fields.wattProduction_day, 0.0);
        this.put(Fields.current_module_id, null);
        this.put(Fields.electricProductions, new TreeSet<String>()); // electric_production_id
        this.put(Fields.friends, new TreeSet<String>()); // electric_production_id
        this.put(Fields.conversations, new TreeSet<String>()); // electric_production_id
        this.put(Fields.blocked_users, new TreeSet<String>()); // electric_production_id
    }
    public User(Document doc) {
        super(doc);
    }
}