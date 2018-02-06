package protocol.module;

/**
 * Created by hadrien on 16/03/2017.
 */
public class Protocol {

    public enum Path {
        /**
         * @path: MODULE_GET_IDS:
         * @param: API_KEY, LIST UUID.
         * @return: STATUS, LIST IDS, OPT_COMMANDS.
         */
        MODULE_GET_IDS("/module/get/ids"),

        /**
         * @path: MODULE_PRODUCTION_SEND:
         * @param: API_KEY, MAP MODULE_ID:PRODUCTION.
         * @return: STATUS, OPT_COMMANDS.
         */
        MODULE_PRODUCTION_SEND("/module/production/send"),

        /**
         * @path: MODULE_PAIR_STOP:
         * @param: API_KEY, LIST UUID.
         * @return: STATUS, OPT_COMMANDS.
         */
        MODULE_PAIR_STOP("/module/pair/stop"),
        ;
        public String path;
        Path(String path) {
            this.path = path;
        }
    }

    public enum Field {
        APIKEY("apiKey"),
        UUID("UUID"),
        COMMAND("commande"),
        MODULEIDS("moduleIDS"),
        ;
        public String key;
        Field(String key) {
            this.key = key;
        }
    }

    public enum Command {
        SET_MODULE_ID("setModuleId"),
        ;
        public String key;
        Command(String key) {
            this.key = key;
        }
    }

    public enum Status {
        GENERIC_OK("001", "ok"),
        MISC_RANDOM("666", "Random error"),
        ;
        public String code;
        public String message;
        Status(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
