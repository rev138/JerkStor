package org.hzsogood.jerkstor.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.util.*;

@Service
public final class GridFSServiceImpl implements GridFSService {
    @Autowired
    private ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
    private GridFsOperations gridOperations = (GridFsOperations) ctx.getBean("gridFsTemplate");

    // store a file with a custom name and metadata
    @Override
    @Transactional
    public String store(MultipartFile file, String name, HashMap metaData) throws IOException {
        DBObject metaDataObj = new BasicDBObject();

        // Convert the metaData HashMap to a DBObject
        Set<String> keys = metaData.keySet();

        for (String key : keys) {
            metaDataObj.put( key, metaData.get( key ) );
        }

        GridFSFile resultFile = gridOperations.store(file.getInputStream(), name, file.getContentType(), metaDataObj);

        return resultFile.getId().toString();
    }

    // store a file with the original name and metadata
    @Override
    public String store(MultipartFile file, HashMap metaData) throws IOException {
        String name = file.getOriginalFilename();

        return this.store(file, name, metaData);
    }

    // store a file with a custom name and no metadata
    @Override
    public String store(MultipartFile file, String name) throws IOException {
        HashMap metaData = new HashMap();

        return this.store(file, name, metaData);
    }

    // store a file with the original name and no metadata
    @Override
    public String store(MultipartFile file) throws IOException {
        HashMap metaData = new HashMap();
        String name = file.getOriginalFilename();

        return this.store(file, name, metaData);
    }

    // retreive a list of file objects
    @Override
    @Transactional(readOnly = true)
    public List<GridFSDBFile> find(Query query) throws IOException {
        return gridOperations.find( query );
    }

    // retrieve one file using an arbitrary query
    @Override
    @Transactional(readOnly = true)
    public GridFSDBFile findOne(Query query) throws IOException {
        return gridOperations.findOne( query );
    }

    // retrieve one file by OID
    @Override
    @Transactional(readOnly = true)
    public GridFSDBFile findById( String oid ) throws IOException {
        return gridOperations.findOne( new Query().addCriteria( Criteria.where("_id").is( oid ) ) );
    }

    // retrieve a list of files by tag
    @Override
    @Transactional(readOnly = true)
    public List<GridFSDBFile> findByTag ( String tag ) throws IOException {
        return this.find(new Query(Criteria.where("metadata.tags").is(tag)));
    }

    // retrieve a list of files by multiple tags
    @Override
    @Transactional(readOnly = true)
    public List<GridFSDBFile> findByAllTags ( List<String> tags ) throws IOException {
        return this.find(new Query( Criteria.where("metadata.tags").all(tags)));
    }

    // retrieve a list of files by multiple tags
    @Override
    @Transactional(readOnly = true)
    public List<GridFSDBFile> findByAnyTags ( List<String> tags ) throws IOException {
        return this.find(new Query( Criteria.where("metadata.tags").in(tags)));
    }

    // check if a file with this name and path already exists
    @Transactional(readOnly = true)
    boolean fileExists ( String fileName, String path ) throws IOException {
        GridFSDBFile result = gridOperations.findOne(new Query().addCriteria(Criteria.where("filename").is(fileName)).addCriteria(Criteria.where("metadata.path").is(path)));

        return ( result != null );
    }

    // calculate the md5 hash for the file
    @Transactional(readOnly = true)
    String getMD5 ( MultipartFile file ) throws IOException {
        file.getInputStream().mark(Integer.MAX_VALUE);
        String md5 = DigestUtils.md5Hex( file.getInputStream() );
        file.getInputStream().reset();

        return md5;
    }
}

