package protocol.mobile;

/**
 * Created by hadrien on 16/03/2017.
 */
public class Protocol {

    public enum Path {
        /**
         * @path: REGISTRATION:
         * @param: LOGIN, PASSWORD, FIRSTNAME, LASTNAME, PHONE, EMAIL.
         * @return: STATUS, TOKEN.
         */
        REGISTRATION("/registration"),

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
        USER_GET_PROFILE("/user/get/profile"),

        /**
         * @path: USER_GET_PROFILE:
         * @param: TOKEN.
         * @return: STATUS, PICTURE.
         */
        USER_GET_PICTURE("/user/get/picture"),

        /**
         * @path: USER_UPDATE_PICTURE:
         * @param: TOKEN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         * @return: STATUS.
         */
        USER_UPDATE_PROFILE("/user/update/profile"),

        /**
         * @path: USER_UPDATE_PASSWORD:
         * @param: TOKEN, PASSWORD, NEW_PASSWORD.
         * @return: STATUS, TOKEN.
         */
        USER_UPDATE_PASSWORD("/user/update/password"),

        /**
         * @path: USER_UPDATE_PICTURE:
         * @param: TOKEN, PICTURE.
         * @return: STATUS.
         */
        USER_UPDATE_PICTURE("/user/update/picture"),

        /**
         * @path: USER_PAIR_START:
         * @param: TOKEN, MODULE_ID.
         * @return: STATUS.
         */
        USER_PAIR_START("/user/pair/start"),

        /**
         * @path: USER_PAIR_STOP:
         * @param: TOKEN.
         * @return: STATUS.
         */
        USER_PAIR_STOP("/user/pair/stop"),

        /**
         * @path: USER_GET_INSTANTPRODUCTION:
         * @param: TOKEN.
         * @return: STATUS, LIST PRODUCTION.
         */
        USER_GET_INSTANTPRODUCTION("/user/get/instantprodution"),
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
        MODULEID("moduleID"),
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
        NO_SPORT_SESSION("402", "No sport session"),
        ;
        public String code;
        public String message;
        Status(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
