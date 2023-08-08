package poormanshttpserver.client;

import java.util.HashMap;
import java.util.Map;

public class ClientRequest {
    public String method;
    public String protocolVersion;
    public String path;
    public Map<String, String> headers = new HashMap<>();
    public String entity;
}
