package com.poc.global.search.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.validation.constraints.NotNull;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class MongoDBConfiguration extends AbstractMongoClientConfiguration {

  @Lazy
  @Autowired
  private Environment env;

  @Value("${spring.data.mongodb.host}")
  private String host;

  @Value("${spring.data.mongodb.port}")
  private String port;

  @Value("${spring.data.mongodb.database}")
  private String databaseName;

  @NotNull
  @Override
  public String getDatabaseName() {
    return databaseName;
  }

  @NotNull
  @Override
  public MongoClient mongoClient() {
    return MongoClients.create(MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.STANDARD)
            .applyConnectionString(new ConnectionString(buildConnectionString())).build());
  }

  private String buildConnectionString() {

    if (env == null)
      throw new RuntimeException("Enviroment not Started error");

    String profile = "";
    List<String> profiles = Arrays.asList(env.getActiveProfiles());

    if (profiles.contains("dev")) {
      profile = "dev";
    } else if (profiles.contains("stg")) {
      profile = "stg";
    } else if (profiles.contains("prod")) {
      profile = "prod";
    }

    return switch (profile) {
      case "stg" -> "mongodb://nip_staging:" + "password" + "@" + host + ":" + port + "/nip_staging?directConnection=true";
      case "prod" -> "mongodb://nip_production:" + "password" + "@" + host + ":" + port + "/nip_production?directConnection=true";
      default -> //Dev
        "mongodb://" + host + ":" + port;
    };
  }
}
