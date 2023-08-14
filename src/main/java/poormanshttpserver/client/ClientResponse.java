package poormanshttpserver.client;

import java.util.HashMap;
import java.util.Map;

public class ClientResponse {
    public String httpProtocolVersion;
    public String httpResponseCode;
    public Map<String, String> httpHeaders = new HashMap<>();
    public byte[] entity;
}
