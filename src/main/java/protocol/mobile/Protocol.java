package protocol.mobile;

/**
 * Created by hadrien on 16/03/2017.
 */
public class Protocol {

    public enum Path {
        REGISTRATION("/registration"),
        AUTHENTICATION("/authentication"),
        AUTHENTICATION_TOKEN("/authentication/token"),
        USER_GET_PROFILE("/user/get/profile"),
        USER_GET_PICTURE("/user/get/picture"),
        USER_UPDATE_PROFILE("/user/update/profile"),
        USER_UPDATE_PASSWORD("/user/update/password"),
        USER_UPDATE_PICTURE("/user/update/picture"),
        USER_PAIR_START("/user/pair/start"),
        USER_PAIR_STOP("/user/pair/stop"),
        USER_GET_INSTANTPRODUCTION("/user/get/instantproduction"),
        GET_EVENTS("/get/events"),
        GET_EVENTPREVIEW("/get/eventpreview"),
        GET_EVENTSUSERS("/get/eventusers"),
        AFFILIATE("/affiliate"),
        GET_AFFILIATION("/get/affiliation"),
        GET_POSTS("/get/posts"),
        GET_POSTCONTENT("/get/postcontent"),
        EVENT_REGISTRATION("/event/registration"),
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
        TARGETID("target id"),
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
        POST_NOT_FOUND("413", "Post not found"),
        CENTER_NOT_FOUND("414", "Fitness center not found"), 
        EVENT_NOT_FOUND("413", "Event not found"),
        ;
        public String code;
        public String message;
        Status(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
