package protocol.mobile;

/**
 * Created by hadrien on 16/03/2017.
 */
public class Protocol {

    public enum Path {
        /**
         * @param: LOGIN, PASSWORD, FIRSTNAME, LASTNAME, PHONE, EMAIL.
         * @return: STATUS, TOKEN.
         */
        REGISTRATION("/registration"),

        /**
         * @param: LOGIN, PASSWORD.
         * @return: STATUS, TOKEN.
         */
        AUTHENTICATION("/authentication"),

        /**
         * @param: TOKEN.
         * @return: STATUS.
         */
        AUTHENTICATION_TOKEN("/authentication/token"),

        /**
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        USER_GET_PROFILE("/user/get/profile"),

        /**
         * @param: TOKEN.
         * @return: STATUS, PICTURE.
         */
        USER_GET_PICTURE("/user/get/picture"),

        /**
         * @param: TOKEN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         * @return: STATUS.
         */
        USER_UPDATE_PROFILE("/user/update/profile"),

        /**
         * @param: TOKEN, PASSWORD, NEW_PASSWORD.
         * @return: STATUS, TOKEN.
         */
        USER_UPDATE_PASSWORD("/user/update/password"),

        /**
         * @param: TOKEN, PICTURE.
         * @return: STATUS.
         */
        USER_UPDATE_PICTURE("/user/update/picture"),

        /**
         * @param: TOKEN, MODULE_ID.
         * @return: STATUS.
         */
        USER_PAIR_START("/user/pair/start"),

        /**
         * @param: TOKEN.
         * @return: STATUS.
         */
        USER_PAIR_STOP("/user/pair/stop"),

        /**
         * @param: TOKEN.
         * @return: STATUS, LIST PRODUCTION.
         */
        USER_GET_INSTANTPRODUCTION("/user/get/instantproduction"),

        /**
         * @param: TOKEN, START, END.
         * @return: STATUS, LIST EVENTS.
         */
        GET_EVENTS("/get/events"),

        /**
         * @param: TOKEN, EVENT_ID.
         * @return: STATUS, DESCRIPTION, BASE64IMAGE, START_DATE, END_DATE, USER_REGISTERED.
         */
        GET_EVENTPREVIEW("/get/eventpreview"),

        /**
         * @param: TOKEN, EVENT_ID.
         * @return: STATUS, LIST USERS_LOGIN.
         */
        GET_EVENTSUSERS("/get/eventusers"),

        /**
         * @param: TOKEN, AFFILIATIONTOKEN.
         * @return: STATUS, SPORTCENTERID.
         */
        AFFILIATE("/affiliate"),

        /**
         * @param: TOKEN.
         * @return: STATUS, SPORTCENTERID.
         */
        GET_AFFILIATION("/get/affiliation"),

        /**
         * @param: TOKEN, TARGERID, START, END.
         * @return: STATUS, LIST POSTID.
         */
        GET_POSTS("/get/posts"),

        /**
         * @param: TOKEN, POSTID.
         * @return: STATUS, POSTTYPE, POSTICON, POSTDATE, POSTCONTENT.
         */
        GET_POSTCONTENT("/get/postcontent"),
        ;
        public String path;
        Path(String path) {
            this.path = path;
        }
    }

    public enum Field {
        LOGIN("login"),
        PASSWORD("password"),
        NEW_PASSWORD("new password"),
        TOKEN("token"),
        STATUS("code"),
        FIRSTNAME("first name"),
        LASTNAME("last name"),
        PHONE("phone number"),
        EMAIL("email address"),
        PICTURE("picture"),
        PRODUCTION("production"),
        SESSIONID("session id"),
        START("start"),
        END("end"),
        EVENTS("events"),
        EVENTID("eventid"),
        EVENTDESCRIPTION("description"),
        EVENTPICTURE("picture"),
        EVENTSTARTDATE("start date"),
        EVENTENDDATE("end date"),
        EVENTUSERREGISTERED("user_registered"),
        EVENTUSERS("users"),
        AFFILIATIONTOKEN("affiliation token"),
        SPORTCENTERID("sport center id"),
        TARGERID("target id"),
        POSTS("posts"),
        POSTID("post id"),
        POSTTYPE("post type"),
        POSTICON("post icon"),
        POSTDATE("post date"),
        POSTCONTENT("post content"),
        ;
        public String key;
        Field(String key) {
            this.key = key;
        }
    }

    public enum Status {
        GENERIC_OK("001", "ok"),
        REG_SUCCESS("101", "registration successful"),
        REG_ERROR_LOGIN_TAKEN("301", "registration failed, login already taken"),
        REG_ERROR_LOGIN("302", "registration failed, bad login"),
        REG_ERROR_PASSWORD("303", "registration failed, bad password"),
        AUTH_SUCCESS("201", "authentication successful"),
        AUTH_ERROR_TOKEN("202", "authentication failed, bad token"),
        AUTH_ERROR_CREDENTIALS("501", "authentication failed, bad credentials"),
        GENERIC_KO("401", "ko"),
        INTERNAL_SERVER_ERROR("666", "Random error"),
        SPORT_SESSION_NO_SESSION("410", "No sport session"),
        SPORT_SESSION_BAD_SESSIONID("411", "Bad session id"),
        NO_AFFILIATION("412", "Not affiliated with sport center"),
        POST_NOT_FOUND("413", "Post not found");
        public String code;
        public String message;
        Status(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
