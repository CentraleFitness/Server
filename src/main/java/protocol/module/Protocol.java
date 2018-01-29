package protocol.module;

/**
 * Created by hadrien on 16/03/2017.
 */
public class Protocol {

    public enum Path {
        /**
         * @path: MODULE_GET_IDS:
         * @param: API_KEY, UUID LIST.
         * @return: STATUS, IDS LIST, OPT_COMMANDS.
         */
        REGISTRATION("/module/get/ids"),
        ;
        public String path;
        Path(String path) {
            this.path = path;
        }
    }

    public enum Field {
        ;
        public String key;
        Field(String key) {
            this.key = key;
        }
    }

    public enum Status {
        ;
        public String code;
        public String message;
        Status(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
