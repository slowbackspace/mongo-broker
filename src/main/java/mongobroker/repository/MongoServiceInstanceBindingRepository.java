package mongobroker.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import mongobroker.model.ServiceInstanceBinding;

/**
 * Repository for ServiceInstanceBinding objects
 *
 * @author sgreenberg@pivotal.io
 *
 */
public interface MongoServiceInstanceBindingRepository extends MongoRepository<ServiceInstanceBinding, String> {

}