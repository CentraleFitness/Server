package protocol.admin;

public class Protocol {
    public enum Path {


        PASSWORD("/password"),

        ACCOUNT("/account"),

        AUTHENTICATION("/authentication"),

        AUTHENTICATION_TOKEN("/authenticationtoken"),

        MANAGER("/manager"),

        MANAGER_ACTIVITY("/manager_activity"),

        USER_ACTIVITY("/user_activity"),

        MANAGER_UNDO_REFUSE ("/manager_undo_refuse"),

        MANAGER_FEEDBACK("/manager_feedback"),

        FEEDBACK_RESPONSE("/feedback_response"),

        FEEDBACK_STATE("/feedback_state"),

        MOBILE_FEEDBACK("/mobile_feedback"),

        FITNESS_CENTER("/fitness_center"),

        MODULE("/module"),

        USER("/user"),

        MODULE_STATE("/module_state"),

        CONSULT_SIRET("/consult_siret"),

        ;
        public String path;
        Path(String path) {
            this.path = path;
        }
    }

    public enum Field {
        EMAIL("email"),
        PHONE("phone_number"),
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
        MANAGERS("managers"),
        MODULES("modules"),
        FEEDBACKS("feedbacks"),
        FITNESS_CENTER_MANAGER_ID("fitness_center_manager_id"),
        IS_VALIDATED("is_validated"),
        IS_ACTIVE("is_active"),
        FEEDBACK_ID("feedback_id"),
        FITNESS_CENTERS("fitness_centers"),
        ADMINISTRATOR_NAME("administrator_name"),
        MODULE_STATES("module_states"),
        CREATOR_ADMIN_ID("creator_admin_id"),
        CREATOR_ADMIN_NAME("creator_admin_name"),
        AUTO_GENERATE_UUID("auto_generate_uuid"),
        MODULE_STATE_CODE("module_state_code"),
        MODULE_STATE_ID("module_state_id"),
        SIRET("siret"),
        INFO("info"),
        USERS("users"),
        CENTERS("centers"),
        USER_ID("user_id"),
        CONTENT("content"),
        FEEDBACK_STATE_CODE("feedback_state_code"),
        FEEDBACK_STATES("feedback_states"),
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
        REG_ERROR_PHONE("306", "registration failed, bad phone"),
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
