package org.hzsogood.jerkstor.service;

import org.springframework.data.mongodb.core.query.Query;

public interface MongoService {

    public long count ( Query query, String string );

}
