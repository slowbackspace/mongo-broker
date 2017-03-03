package mongobroker.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maros on 19/08/16.
 */
public class ServiceInstanceBinding {


    private String id;
    private String serviceInstanceId;
    private Map<String, Object> credentials = new HashMap<>();
    private String syslogDrainUrl;
    private String appGuid;

    public ServiceInstanceBinding(String id, String serviceInstanceId, Map<String, Object> credentials,
                                  String syslogDrainUrl, String appGuid) {
        this.id = id;
        this.serviceInstanceId = serviceInstanceId;
        this.credentials = credentials;
        this.syslogDrainUrl = syslogDrainUrl;
        this.appGuid = appGuid;
    }

    public String getId() {
        return id;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public Map<String, Object> getCredentials() {
        return credentials;
    }

    public String getSyslogDrainUrl() {
        return syslogDrainUrl;
    }

    public String getAppGuid() {
        return appGuid;
    }

    public void setCredentials(Map<String, Object> credentials) {
        if (credentials == null) {
            this.credentials = new HashMap<>();
        } else {
            this.credentials = credentials;
        }
    }
}
