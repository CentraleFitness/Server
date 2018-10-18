package protocol.intranet;

public class Protocol {

    public enum Path {

        /**
         * @path: REGISTRATION:
         * @param: EMAIL, PASSWORD
         * @return: STATUS, TOKEN.
         */
        REGISTRATION("/registration"),

        /**
         * @path: REGISTRATION:
         * @param: EMAIL, PASSWORD
         * @return: STATUS, TOKEN.
         */
        MANAGER_REGISTER("/manager/register"),

        /**
         * @path: AUTHENTICATION:
         * @param: LOGIN, PASSWORD.
         * @return: STATUS, TOKEN.
         */
        AUTHENTICATION("/authentication"),

        /**
         * @path: AUTHENTICATION_TOKEN:
         * @param: TOKEN.
         * @return: STATUS.
         */
        AUTHENTICATION_TOKEN("/authentication/token"),

        /**
         * @path: USER_GET_PROFILE:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        MANAGER_GET_PROFILE("/manager/get/profile"),

        /**
         * @path: USER_GET_PROFILE:
         * @param: TOKEN.
         * @return: STATUS, PICTURE.
         */
        MANAGER_GET_PICTURE("/manager/get/picture"),

        /**
         * @path: USER_UPDATE_PICTURE:
         * @param: TOKEN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         * @return: STATUS.
         */
        MANAGER_UPDATE_PROFILE("/manager/update/profile"),

        /**
         * ! NOT IMPLEMENTED YET !
         *
         * @path: USER_UPDATE_PASSWORD:
         * @param: TOKEN, PASSWORD, NEW_PASSWORD.
         * @return: STATUS, TOKEN.
         */
        MANAGER_UPDATE_PASSWORD("/manager/update/password"),

        /**
         * @path: USER_UPDATE_PICTURE:
         * @param: TOKEN, PICTURE.
         * @return: STATUS.
         */
        MANAGER_UPDATE_PICTURE("/manager/update/picture"),

        /**
         * @path: CENTER_REGISTER:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        CENTER_REGISTER("/club/register"),

        /**
         * @path: CENTER_GET_PROFILE:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        CENTER_GET_PROFILE("/club/get/profile"),

        /**
         * @path: CENTER_GET_PROFILE:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        CENTER_GET_PICTURE("/club/get/picture"),

        /**
         * @path: CENTER_GET_PROFILE:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        CENTER_UPDATE_PICTURE("/club/update/picture"),

        /**
         * @path: CENTER_GET_PROFILE:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        CENTER_UPDATE_PROFILE("/club/update/profile"),

        /**
         * @path: CENTER_GET_PROFILE:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        CENTER_INCREASE_ALBUM("/club/increase/album"),

        /**
         * @path: CENTER_GET_PROFILE:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        CENTER_DECREASE_ALBUM("/club/decrease/album"),

        /**
         * @path: CENTER_GET_PROFILE:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        CENTER_GET_ALBUM("/club/get/album"),

        /**
         * @path: CENTER_GET_PROFILE:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        CENTER_ADD_PUBLICATION("/club/add/publication"),

        /**
         * @path: CENTER_GET_PROFILE:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        CENTER_DELETE_PUBLICATION("/club/delete/publication"),

        /**
         * @path: CENTER_GET_PROFILE:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        CENTER_GET_PUBLICATIONS("/club/get/publications"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        ADD_FEEDBACK("/manager/add/feedback"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        GET_FEEDBACKS("/manager/get/feedbacks"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        GET_FEEDBACK_STATES("/manager/get/feedbackstates"),

        /**
         * @path: ADD_EVENT:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        ADD_EVENT("/center/add/event"),

        /**
         * @path: ADD_EVENT:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        UPDATE_EVENT("/center/update/event"),

        /**
         * @path: ADD_EVENT:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        DELETE_EVENT("/center/delete/event"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        GET_EVENTS("/center/get/events"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        POST_EVENT("/center/post/event"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        SET_CUSTOM_PROGRAMS_AVAILABILITY("/center/set/customprogramavailability"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        ADD_CUSTOM_PROGRAM("/center/add/customprogram"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        UPDATE_CUSTOM_PROGRAM("/center/update/customprogram"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        GET_CUSTOM_PROGRAMS("/center/get/customprograms"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        DELETE_CUSTOM_PROGRAM("/center/delete/customprogram"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        GET_ACTIVITIES("/center/get/activities"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        GET_HOME_SUMMARY("/center/get/homesummary"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        GET_DISPLAY_CONFIGURATION("/center/get/displayconfiguration"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        UPDATE_DISPLAY_CONFIGURATION("/center/update/displayconfiguration"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        GET_MODULES("/center/get/modules"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        GET_MODULE_STATES("/center/get/modulestates"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        GET_STATISTICS("/center/get/statistics"),

        /**
         * @path: ADD_FEEDBACK:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        GET_FITNESS_CENTER_ID("/center/get/id"),


        ;
        public String path;
        Path(String path) {
            this.path = path;
        }
    }

    public enum Field {
        PASSWORD("password"),
        NEW_PASSWORD("new_password"),
        TOKEN("token"),
        STATUS("code"),
        FIRSTNAME("first_name"),
        LASTNAME("last_name"),
        PHONE("phone_number"),
        CENTER_PHONE("center_phone_number"),
        PICTURE("picture"),
        EMAIL("email"),
        NAME("name"),
        DESCRIPTION("description"),
        ADDRESS("address"),
        ADDRESS_SECOND("address_second"),
        ZIP_CODE("zip_code"),
        CITY("city"),
        TITLE("title"),
        ALBUM("album"),
        PICTURE_ID("picture_id"),
        TEXT("text"),
        CREATION_DATE("creation_date"),
        PUBLICATIONS("publications"),
        PUBLICATION_ID("publication_id"),
        UPDATE_DATE("update_date"),
        START_DATE("start_date"),
        END_DATE("end_date"),
        FEEDBACK_ID("feedback_id"),
        EVENT_ID("event_id"),
        FEEDBACKS("feedbacks"),
        EVENTS("events"),
        FEEDBACK_STATES("feedback_states"),
        NB_SUBSCRIBERS("nb_subscribers"),
        LAST_POST("last_post"),
        DELETION_CAUSE("deletion_cause"),
        CUSTOM_PROGRAMS("custom_programs"),
        NB_ACTIVITIES("nb_activities"),
        TOTAL_TIME("total_time"),
        AVAILABLE("available"),
        ACTIVITIES("activities"),
        CUSTOM_PROGRAM_ID("custom_program_id"),
        ENABLED("enabled"),
        DISABLED("disabled"),
        FITNESS_CENTER_ID("fitness_center_id"),
        SHOW_EVENTS("show_events"),
        SHOW_NEWS("show_news"),
        SHOW_GLOBAL_PERFORMANCES("show_global_performances"),
        PERFORMANCES_TYPE("performances_type"),
        SHOW_RANKING_DISCIPLINE("show_ranking_discipline"),
        SHOW_GLOBAL_RANKING("show_global_ranking"),
        SHOW_NATIONAL_PRODUCTION_RANKING("show_national_production_rank"),
        SELECTED_EVENTS("selected_events"),
        SELECTED("selected"),
        NEWS_TYPE("news_type"),
        RANKING_DISCIPLINE_TYPE("ranking_discipline_type"),
        MODULES("modules"),
        MODULE_STATES("module_states"),
        PRODUCTION_DAY("production_day"),
        PRODUCTION_MONTH("production_month"),
        FREQUENTATION_DAY("frequentation_day"),
        FREQUENTATION_MONTH("frequentation_month"),
        CENTER_NAME("center_name"),
        MANAGER_FIRST_NAME("manager_first_name"),
        MANAGER_LAST_NAME("manager_last_name"),
        SIRET("siret"),
        ;
        public String key;
        Field(String key) {
            this.key = key;
        }
    }

    public enum Status {
        GENERIC_OK("001", "ok"),
        REG_SUCCESS("101", "registration successful"),
        REG_ERROR_EMAIL_TAKEN("301", "registration failed, email already taken"),
        REG_ERROR_EMAIL("302", "registration failed, bad email"),
        REG_ERROR_PASSWORD("303", "registration failed, bad password"),
        REG_ERROR_FIRSTNAME("304", "registration failed, bad firstname"),
        REG_ERROR_LASTNAME("305", "registration failed, bad lastname"),
        AUTH_SUCCESS("201", "authentication successful"),
        AUTH_ERROR_TOKEN("202", "authentication failed, bad token"),
        AUTH_ERROR_ACCOUNT_INACTIVE("203", "authentication failed, your account has been set to inactive"),
        AUTH_ERROR_ACCOUNT_NOT_YET_VALIDATED("204", "authentication failed, your account has not been validated yet"),
        AUTH_ERROR_ACCOUNT_REFUSED("205", "authentication failed, your account has been refused"),
        AUTH_ERROR_CREDENTIALS("501", "authentication failed, bad credentials"),
        MGR_ERROR_ERROR_FIRSTNAME("701", "manager's first name is missing"),
        MGR_ERROR_ERROR_LASTNAME("702", "manager's last name is missing"),
        MGR_ERROR_ERROR_PHONE("703", "manager's phone number is missing"),
        MGR_ERROR_NO_CENTER("704", "manager do not have associated center"),
        CTR_REG_SUCCESS("801", "ok"),
        CTR_ERROR_ERROR_NAME("802", "center's name is missing"),
        CTR_ERROR_ERROR_SIRET("808", "center's SIRET number is missing"),
        CTR_ERROR_ERROR_DESCRIPTION("803", "center's description is missing"),
        CTR_ERROR_ERROR_ADDRESS("804", "center's address is missing"),
        CTR_ERROR_ERROR_ZIP_CODE("805", "center's zip code is missing"),
        CTR_ERROR_ERROR_CITY("806", "center's city is missing"),
        CTR_ERROR_ALREADY_EXISTS("807", "center already exists"),
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
