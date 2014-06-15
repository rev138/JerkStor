package org.hzsogood.jerkstor.service;

import org.hzsogood.jerkstor.config.SpringMongoConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public final class MongoServiceImpl implements MongoService {
    @Autowired
    private ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
    private MongoOperations mongoOperations = (MongoOperations) ctx.getBean("mongoTemplate");

    @Override
    public long count(Query query, String string) {
        return 0;
    }
}