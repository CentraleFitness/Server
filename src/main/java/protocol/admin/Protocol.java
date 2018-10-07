package protocol.admin;

public class Protocol {
    public enum Path {


        PASSWORD("/password"),

        ACCOUNT("/account"),

        AUTHENTICATION("/authentication"),

        AUTHENTICATION_TOKEN("/authenticationtoken"),

        MANAGER("/manager"),

        MANAGER_FEEDBACK("/managerfeedback"),

        MODULE("/module"),

        ;
        public String path;
        Path(String path) {
            this.path = path;
        }
    }

    public enum Field {
        EMAIL("email"),
        FIRSTNAME("first_name"),
        LASTNAME("last_name"),
        PASSWORD("password"),
        NEW_PASSWORD("new_password"),
        TOKEN("token"),
        STATUS("code"),
        ADMINISTRATOR_ID("administrator_id"),
        FITNESS_CENTER_ID("fitness_center_id"),
        MODULE_ID("module_id"),
        MACHINE_TYPE("machine_type"),
        UUID("uuid"),
        ACCOUNTS("accounts"),
        MODULES("modules"),
        FEEDBACKS("feedbacks"),
        FITNESS_CENTER_MANAGER_ID("fitness_manager_id"),
        IS_VALIDATED("is_validated"),
        FEEDBACK_ID("feedback_id"),
        ;
        public String key;
        Field(String key) {
            this.key = key;
        }
    }

    public enum Status {
        GENERIC_OK("001", "ok"),
        REG_SUCCESS("101", "registration successful"),
        AUTH_SUCCESS("201", "authentication successful"),
        AUTH_ERROR_TOKEN("202", "authentication failed, bad token"),
        AUTH_ERROR_CREDENTIALS("501", "authentication failed, bad credentials"),
        REG_ERROR_EMAIL_TAKEN("301", "registration failed, email already taken"),
        REG_ERROR_EMAIL("302", "registration failed, bad email"),
        REG_ERROR_PASSWORD("303", "registration failed, bad password"),
        REG_ERROR_FIRSTNAME("304", "registration failed, bad firstname"),
        REG_ERROR_LASTNAME("305", "registration failed, bad lastname"),
        MISC_ERROR("401", "database problem"),
        GENERIC_MISSING_PARAM("402", "Missing parameters"),
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
