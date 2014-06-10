package org.hzsogood.jerkstor.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.hzsogood.jerkstor.config.SpringMongoConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

@Service
public final class GridFSServiceImpl implements GridFSService {
    @Autowired
    private ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
    private GridFsOperations gridOperations = (GridFsOperations) ctx.getBean("gridFsTemplate");

    // store a file with a custom name and metadata
    @Override
    @Transactional
    public String store(MultipartFile file, String name, Hashtable<String, String> metaData) throws IOException {
        DBObject metaDataObj = new BasicDBObject();

        // Convert the metaData HashMap to a DBObject
        Set<String> keys = metaData.keySet();

        // TODO: drop '_class' key
        for (String key : keys) {
            metaDataObj.put(key, metaData.get(key));
        }
        GridFSFile resultFile = gridOperations.store(file.getInputStream(), name, file.getContentType(), metaData);

        return resultFile.getId().toString();
    }

    // store a file with the original name and metadata
    @Override
    public String store(MultipartFile file, Hashtable<String, String> metaData) throws IOException {
        String name = file.getOriginalFilename();

        return this.store(file, name, metaData);
    }

    // store a file with a custom name and no metadata
    @Override
    public String store(MultipartFile file, String name) throws IOException {
        Hashtable<String, String> metaData = new Hashtable<String, String>();

        return this.store(file, name, metaData);
    }

    // store a file with the original name and no metadata
    @Override
    public String store(MultipartFile file) throws IOException {
        Hashtable<String, String> metaData = new Hashtable<String, String>();
        String name = file.getOriginalFilename();

        return this.store(file, name, metaData);
    }

    // retreive a list of file objects
    @Override
    @Transactional(readOnly = true)
    public List<GridFSDBFile> find(Query query) throws IOException {
        return gridOperations.find( query );
    }

    // retreive one file using an arbitrary query
    @Override
    @Transactional(readOnly = true)
    public GridFSDBFile findOne(Query query) throws IOException {
        return gridOperations.findOne( query );
    }

    // retreive one file by OID
    @Override
    @Transactional(readOnly = true)
    public GridFSDBFile findById( String oid ) throws IOException {
        return gridOperations.findOne( new Query().addCriteria( Criteria.where("_id").is( oid ) ) );
    }
}