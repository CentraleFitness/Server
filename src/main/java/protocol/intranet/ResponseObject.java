package protocol.intranet;

import java.util.HashMap;
import java.util.Objects;

public class ResponseObject extends HashMap<String, Object> {


    public ResponseObject(boolean isAnError){
        put("error", String.valueOf(isAnError));
    }

    public boolean isAnError(){
        if (!containsKey("error"))
            return true;
        return (Objects.equals(get("error"), "true"));
    }


}