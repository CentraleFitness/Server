package protocol.image;

/**
 * Created by hadrien on 16/03/2017.
 */
public class Protocol {

    public enum Path {
        /**
         * @param: NA.
         * @return: STATUS.
         */
        STORE("/store"),

        /**
         * @param: NA.
         * @return: STATUS.
         */
        GET("/get"),

        /**
         * @param: NA.
         * @return: STATUS.
         */
        DELETE("/delete"),

        /**
         * @param: NA.
         * @return: STATUS.
         */
        GENERATE_TEMPORARY_URL("/generatetemporaryurl"),
        ;
        public String path;
        Path(String path) {
            this.path = path;
        }
    }

    public enum Field {
        STATUS("code"),
        ;
        public String key;
        Field(String key) {
            this.key = key;
        }
    }

    public enum Status {
        GENERIC_OK("001", "ok"),
        GENERIC_KO("401", "ko"),
        INTERNAL_SERVER_ERROR("666", "Random error"),
        ;
        public String code;
        public String message;
        Status(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
