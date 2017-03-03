package mongobroker.service;

import mongobroker.model.ServiceInstanceBinding;
import mongobroker.repository.MongoServiceInstanceBindingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;


@Service
public class MongoServiceInstanceBindingService implements ServiceInstanceBindingService{


    private MongoAdminService mongo;
    private MongoServiceInstanceBindingRepository bindingRepository;

    @Autowired
    public MongoServiceInstanceBindingService(MongoAdminService mongo,
                                              MongoServiceInstanceBindingRepository bindingRepository) {
        this.mongo = mongo;
        this.bindingRepository = bindingRepository;
    }

    @Override
    public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
        String bindingId = request.getBindingId();
        String serviceInstanceId = request.getServiceInstanceId();
        String appGuid = request.getBoundAppGuid();

        ServiceInstanceBinding binding = bindingRepository.findOne(bindingId);
        if (binding != null) {
            throw new ServiceInstanceBindingExistsException(serviceInstanceId, bindingId);
        }

        String database = serviceInstanceId;
        String username = bindingId;

        String password = UUID.randomUUID().toString();

        // TODO check if user already exists in the DB
        if (mongo.existsUser(database, username))
            throw new IllegalArgumentException("User already exists.");

        mongo.createUser(database, username, password);

        Map<String, Object> credentials = Collections.singletonMap(
                "uri", (Object) mongo.getConnectionString(database, username, password)
        );

        binding = new ServiceInstanceBinding(bindingId, serviceInstanceId, credentials, null, request.getBoundAppGuid());
        bindingRepository.save(binding);

        return new CreateServiceInstanceAppBindingResponse().withCredentials(credentials);
    }

    @Override
    public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
        String serviceInstanceId = request.getServiceInstanceId();
        String bindingId = request.getBindingId();
        ServiceInstanceBinding binding = getServiceInstanceBinding(bindingId);

        if (binding == null) {
            throw new ServiceInstanceBindingDoesNotExistException(bindingId);
        }

        mongo.deleteUser(serviceInstanceId, bindingId);
        bindingRepository.delete(bindingId);
    }


    protected ServiceInstanceBinding getServiceInstanceBinding(String id) {
        return bindingRepository.findOne(id);
    }
}
