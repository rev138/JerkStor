package org.hzsogood.jerkstor.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.util.Arrays;

/**
 * Spring MongoDB configuration file
 *
 */

@Configuration
public class SpringMongoConfig extends AbstractMongoConfiguration {

//    public @Bean
//    MongoDbFactory mongoDbFactory() throws Exception {
//        return new SimpleMongoDbFactory(new MongoClient(), "gridtest");
//    }
//
//    public @Bean
//    MongoTemplate mongoTemplate() throws Exception {
//
//        return new MongoTemplate(mongoDbFactory());
//    }

    @Bean
    public GridFsTemplate gridFsTemplate() throws Exception {
        return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
    }

    @Override
    protected String getDatabaseName() {
        return "gridtest";
    }

    @Override
    @Bean
    public Mongo mongo() throws Exception {
        String password = "grid";
        MongoCredential credential = MongoCredential.createMongoCRCredential("grid", "gridtest", password.toCharArray());
        return new MongoClient(new ServerAddress("127.0.0.1"), Arrays.asList(credential));
    }

}