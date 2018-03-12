package protocol.image;

/**
 * Created by hadrien on 16/03/2017.
 */
public class Protocol {

    public enum Path {
        /**
         * @param: TOKEN, B64_PICTURE.
         * @return: PICTURE_ID, STATUS.
         */
        STORE("/store"),

        /**
         * @param: TEMPORARY_URL.
         * @return: B64_PICTURE, STATUS.
         */
        GET("/get"),

        /**
         * @param: TOKEN, PICUTRE_ID.
         * @return: STATUS.
         */
        DELETE("/delete"),

        /**
         * @param: TOKEN, PICUTRE_ID.
         * @return: TEMPORARY_URL, STATUS.
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
        TOKEN("token"),
        TEMPORARY_URL("temporary url"),
        PICUTRE_ID("picture id"),
        B64_PICTURE("base64 picture"),
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
