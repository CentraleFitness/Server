package model.misc;

import model.Database;
import model.entities.User;

import java.lang.reflect.InvocationTargetException;

import static com.mongodb.client.model.Filters.eq;

public class Runnable {

    public static class new_User {
        public static void main(String[] args) throws InstantiationException, IllegalAccessException {
            Database db = Database.getInstance();
            for (Database.Collections col : Database.Collections.values()) db.new_entity(col);
        }
    }

    public static class find_User {
        public static void main(String[] args) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            Database db = Database.getInstance();
            User user = (User) db.find_entity(Database.Collections.Users, User.Field.LOGIN, "tata");
            user.setField(User.Field.EMAIL, "tata@gmail.com");
            System.out.println(user.getField(User.Field.EMAIL));
        }
    }
}
