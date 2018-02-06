package protocol;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by hadrien on 14/03/2017.
 */

public class ResponseObject extends HashMap<String, String> {


    public ResponseObject(boolean isAnError){
        put("error", String.valueOf(isAnError));
    }

    public boolean isAnError(){
        if (!containsKey("error"))
            return true;
        return (Objects.equals(get("error"), "true"));
    }


}