package protocol;

/**
 * Created by hadrien on 16/03/2017.
 */
public class Protocol {

    public enum Path {
        REGISTRATION("/registration"),
        AUTHENTICATION("/authentication"),
        AUTHENTICATION_TOKEN("/authentication/token"),
        ;
        public String path;
        Path(String path) {
            this.path = path;
        }
    }

    public enum Field {
        LOGIN("login"),
        PASSWORD("password"),
        TOKEN("token"),
        STATUS("code"),
        FIRSTNAME("first name"),
        LASTNAME("last name"),
        PHONE("phone number"),
        EMAIL("email address"),
        ;
        public String key;
        Field(String key) {
            this.key = key;
        }
    }

    public enum Status {
        REG_SUCCESS("101", "registration successful"),
        REG_ERROR_LOGIN_TAKEN("301", "registration failed, login already taken"),
        REG_ERROR_LOGIN("302", "registration failed, bad login"),
        REG_ERROR_PASSWORD("303", "registration failed, bad password"),
        AUTH_SUCCESS("201", "authentication successful"),
        AUTH_ERROR_TOKEN("202", "authentication failed, bad token"),
        AUTH_ERROR_CREDENTIALS("501", "authentication failed, bad credentials"),
        MISC_ERROR("401", "database problem"),
        MISC_RANDOM("666", "Random error");
        ;
        public String code;
        public String message;
        Status(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
