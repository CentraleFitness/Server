package protocol;

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
         * @path: USERPROFILE:
         * @param: TOKEN.
         * @return: STATUS, LOGIN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         */
        USERPROFILE("/userprofile"),

        /**
         * @path: USER_UPDATE_PROFILE:
         * @param: TOKEN, FIRSTNAME, LASTNAME, EMAIL, PHONE.
         * @return: STATUS.
         */
        USER_UPDATE_PROFILE("/user/update/profile"),

        /**
         * ! NOT IMPLEMENTED YET !
         *
         * @path: USER_UPDATE_PASSWORD:
         * @param: NA
         * @return: NA
         */
        USER_UPDATE_PASSWORD("/user/update/password"),

        /**
         * ! NOT IMPLEMENTED YET !
         *
         * @path: USER_UPDATE_PICTURE:
         * @param: NA
         * @return: NA
         */
        USER_UPDATE_PICTURE("/user/update/pictire"),

        /**
         * @deprecated
         * ! DO NOT USE !
         *
         * @path: USERWATTPRODUCTIONINSTANT:
         * @param: NA
         * @return: NA
         */
        USERWATTPRODUCTIONINSTANT("/user/watt/instant"),
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
        INSTANTWATT("instant watt"),
        MODULENAME("module name"),
        MACHINETYPE("machine type"),
        PICTURE("picture"),
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
