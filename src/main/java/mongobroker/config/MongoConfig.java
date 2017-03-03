package mongobroker.config;

import com.mongodb.*;
import mongobroker.service.MongoAdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Configuration
@EnableMongoRepositories(basePackages = "mongobroker.repository")
public class MongoConfig extends AbstractMongoConfiguration {

    @Value("${mongodb.host:localhost}")
    private String host;

    @Value("${mongodb.hostsuri:#{null}}")
    private String hostsUri;

    @Value("${mongodb.repsetname:rs1}")
    private String repSetName;

    @Value("${mongodb.port:27017}")
    private int port;

    @Value("${mongodb.username:admin}")
    private String username;

    @Value("${mongodb.password:password}")
    private String password;

    @Value("${mongodb.authdb:admin}")
    private String authSource;

    @Value("${mongodb.metadb:broker-configuration}")
    private String metaDatabase;

    @Override
    // Used by Spring Data MongoDB
    protected String getDatabaseName() {
        return metaDatabase;
    }

    @Override
    public Mongo mongo() throws UnknownHostException {
        return mongoClient();
    }

    @Bean
    public MongoClient mongoClient() throws UnknownHostException {
        final MongoCredential credential = MongoCredential.createScramSha1Credential(username, authSource, password.toCharArray());
        ArrayList<ServerAddress> serverList = new ArrayList<>();

        try {
            String[] hostsWithPort = hostsUri.split(",");

            for (String hostWithPort : hostsWithPort)
                serverList.add(new ServerAddress(hostWithPort.split(":")[0], Integer.parseInt(hostWithPort.split(":")[1])));
        } catch (Exception e) {
            throw new MongoException("Can't parse server list");
        }

        // return new MongoClient(new MongoClientURI("mongodb://"+username+":"+password+"@"+hostsUri+"/?replicaSet="+repSetName));
        MongoClient client =  new MongoClient(serverList, Arrays.asList(credential));
        return client;
    }
}
