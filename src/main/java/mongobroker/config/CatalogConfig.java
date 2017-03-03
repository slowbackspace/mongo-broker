package mongobroker.config;

import mongobroker.exception.MongoServiceException;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.DashboardClient;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;


@Configuration
public class CatalogConfig {
    @Bean
    public Catalog catalog() {
        return catalogFromYaml("catalog.yml");
    }

    private Catalog catalogFromYaml(String filePath) {
        Catalog catalog = new Catalog();

        try {
            InputStream input = new FileInputStream(new File(filePath));
            Yaml yaml = new Yaml();
            LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) yaml.load(input);
            LinkedHashMap<String, Object> properties = (LinkedHashMap<String, Object>) data.get("broker");

            ArrayList<Plan> plans = new ArrayList<Plan>();
            ArrayList<LinkedHashMap<String, Object>> plansProperty = (ArrayList<LinkedHashMap<String, Object>>) properties.get("plans");

            for (Map<String, Object> plan : plansProperty) {
                plans.add(new Plan(
                        plan.get("id").toString(),
                        plan.get("name").toString(),
                        plan.get("description").toString(),
                        (Map<String, Object>) plan.get("metadata"),
                        (Boolean) plan.get("free")
                ));
            }

            LinkedHashMap<String, Object> dashboardClientProperty = (LinkedHashMap<String, Object>) properties.get("dashboard_client");
            DashboardClient dashboardClient = null;

            if (dashboardClientProperty != null) {
                dashboardClient = new DashboardClient(
                        dashboardClientProperty.get("id").toString(),
                        dashboardClientProperty.get("secret").toString(),
                        dashboardClientProperty.get("redirect_uri").toString()
                );
            }


            catalog = new Catalog(Collections.singletonList(
                    new ServiceDefinition(
                            properties.get("id").toString(),
                            properties.get("name").toString(),
                            properties.get("description").toString(),
                            (Boolean) properties.get("bindable"),
                            (Boolean) properties.get("plan_updateable"),
                            plans,
                            (ArrayList<String>) properties.get("tags"),
                            (Map<String, Object>) properties.get("metadata"),
                            (ArrayList<String>) properties.get("requires"),
                            dashboardClient
                    )
            ));
        } catch (FileNotFoundException e) {
            throw new MongoServiceException("Catalog file " + filePath + " not found.");
        }
        return catalog;
    }

}
