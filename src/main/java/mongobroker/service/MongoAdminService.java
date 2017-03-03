package mongobroker.service;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import mongobroker.exception.MongoServiceException;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;

@Service
public class MongoAdminService {

    @Value("${mongodb.authdb:admin}")
    private String adminDatabase;

    @Value("${mongodb.username:admin}")
    private String adminUsername;

    @Value("${mongodb.hostsuri:#{null}}")
    private String hostsUri;

    private Logger logger = LoggerFactory.getLogger(MongoAdminService.class);
    private MongoClient client;

    @Autowired
    public MongoAdminService(MongoClient client) {
        this.client = client;
    }

    // http://stackoverflow.com/a/34270390
    public boolean databaseExists(String databaseName) throws MongoServiceException {
        try {
            MongoCursor<String> allDatabasesCursor = client.listDatabaseNames().iterator();
            while(allDatabasesCursor.hasNext()) {
                if (allDatabasesCursor.next() == databaseName)
                    return true;
            }
            return false;
        } catch (MongoException e) {
            throw handleException(e);
        }
    }

    public void deleteDatabase(String databaseName) throws MongoServiceException {
        try {
            client.getDatabase(adminDatabase);
            client.dropDatabase(databaseName);
        } catch (MongoException e) {
            throw handleException(e);
        }
    }

    public MongoDatabase createDatabase(String databaseName) throws MongoServiceException {
        try {
            MongoDatabase db = client.getDatabase(databaseName);

            // save into a collection to force DB creation.
            MongoCollection<Document> col = db.getCollection("foo");
            Document document = new Document("foo", "bar");

            col.insertOne(document);
            // drop the collection so the db is empty
            col.drop();

            return db;
        } catch (MongoException e){
            // try to clean up and fail
            try {
                deleteDatabase(databaseName);
            } catch (MongoServiceException ignore) {}
            throw handleException(e);
        }
    }

    public void createUser(String database, String username, String password) throws MongoServiceException {
        try {
            MongoDatabase db = client.getDatabase(database);

            Document roles = new Document("role", "readWrite").append("db", database);
            Document command = new Document("createUser", username)
                                            .append("pwd", password)
                                            .append("roles", Collections.singletonList(roles));

            Document result = db.runCommand(command);

            if (result.getDouble("ok").intValue() != 1) {
                MongoServiceException e = new MongoServiceException(result.toString());
                logger.warn(e.getLocalizedMessage());
                throw e;
            }
        } catch (MongoException e) {
            throw handleException(e);
        }
    }

    public void deleteUser(String database, String username) throws MongoServiceException {
        try {
            MongoDatabase db = client.getDatabase(database);
            Document result = db.runCommand(new Document("dropUser", username));

            if (result.getDouble("ok").intValue() != 1) {
                throw handleException(new MongoServiceException(result.toString()));
            }
        } catch (MongoException e) {
            throw handleException(e);
        }
    }

    public boolean existsUser(String database, String username) throws MongoServiceException {
        try {
            //List<MongoCredential> credentials = client.getCredentialsList();
            MongoDatabase db = client.getDatabase(database);
            Document result = db.runCommand(new Document(
                    "usersInfo",
                    new Document("user", username).append("db", database)
            ));

            if (result.getDouble("ok").intValue() != 1) {
                MongoServiceException e = new MongoServiceException(result.toString());
                logger.warn(e.getLocalizedMessage());
                throw e;
            }

            ArrayList users = (ArrayList) result.get("users");

            return users.contains(username);

        } catch (MongoException e) {
            throw handleException(e);
        }
    }

    public String getConnectionString(String database, String username, String password) {
        return new StringBuilder()
                .append("mongodb://")
                .append(username)
                .append(":")
                .append(password)
                .append("@")
                .append(getServerAddresses())
                .append("/")
                .append(database)
                .toString();
    }

    public String getServerAddresses() {
        StringBuilder builder = new StringBuilder();
        for (ServerAddress address : client.getServerAddressList()) {
            builder.append(address.getHost())
                    .append(":")
                    .append(address.getPort())
                    .append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length()-1);
        }
        System.out.println(builder.toString());

        for (ServerAddress address : client.getAllAddress()) {
            System.out.println("host: " + address.getHost());
        }
        return builder.toString();
    }

    private MongoServiceException handleException(Exception e) {
        logger.warn(e.getLocalizedMessage(), e);
        return new MongoServiceException(e.getLocalizedMessage());
    }

}
