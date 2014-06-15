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
import java.util.HashMap;
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
    public String store(MultipartFile file, String name, HashMap<String, Object> metaData) throws IOException {
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
    public String store(MultipartFile file, HashMap<String, Object> metaData) throws IOException {
        String name = file.getOriginalFilename();

        return this.store(file, name, metaData);
    }

    // store a file with a custom name and no metadata
    @Override
    public String store(MultipartFile file, String name) throws IOException {
        HashMap<String, Object> metaData = new HashMap<String, Object>();

        return this.store(file, name, metaData);
    }

    // store a file with the original name and no metadata
    @Override
    public String store(MultipartFile file) throws IOException {
        HashMap<String, Object> metaData = new HashMap<String, Object>();
        String name = file.getOriginalFilename();

        return this.store(file, name, metaData);
    }

    // retrieve a list of file objects
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

    @Override
    @Transactional(readOnly = true)
    public List<GridFSDBFile> findByPath(String path) throws IOException {
        return this.find( new Query( Criteria.where("metadata.path").is(path)));
    }

    @Override
    @Transactional
    public void delete(Query query) throws IOException {
        gridOperations.delete( query );
    }

    @Override
    @Transactional
    public void deleteById(String oid) throws IOException {
        this.delete(new Query().addCriteria( Criteria.where("_id").is( oid ) ));
    }

    @Override
    @Transactional
    public void tagFile(String oid, String tag) throws IOException {
        GridFSDBFile file = this.findById(oid);
        DBObject metaData = file.getMetaData();

        List<String> tags = (List<String>) metaData.get("tags");

        // only add this tag if it's not already there
        if(!tags.contains(tag)){
            tags.add(tag);
            metaData.put("tags", tags);
            file.setMetaData(metaData);
            file.save();
        }
    }

    @Override
    @Transactional
    public void untagFile(String oid, String tag) throws IOException {
        GridFSDBFile file = this.findById(oid);
        DBObject metaData = file.getMetaData();

        List<String> tags = (List<String>) metaData.get("tags");

        if(tags.contains(tag)){
            tags.remove(tag);
            metaData.put("tags", tags);
            file.setMetaData(metaData);
            file.save();
       }

    }

    @Override
    @Transactional
    public void setPath(String oid, String path) throws IOException {
        GridFSDBFile file = this.findById(oid);
        DBObject metaData = file.getMetaData();

        // strip leading/trailing slashes
        path = path.replaceAll( "(^/+|^\\\\+|/+$|\\\\+$)", "" );

        metaData.put("path", path);
        file.setMetaData(metaData);
        file.save();
    }

    @Override
    @Transactional
    public void setName(String oid, String name) throws IOException {
        GridFSDBFile file = this.findById(oid);

        file.put("filename", name);
        file.save();
    }

    // check if a file with this name and path already exists
    @Transactional(readOnly = true)
    boolean fileExists ( String fileName, String path ) throws IOException {
        GridFSDBFile file = gridOperations.findOne(new Query().addCriteria(Criteria.where("filename").is(fileName)).addCriteria(Criteria.where("metadata.path").is(path)));
        return (!(file == null));
    }

    // calculate the md5 hash for the file
    String getMD5 ( MultipartFile file ) throws IOException {
        file.getInputStream().mark(Integer.MAX_VALUE);
        String md5 = DigestUtils.md5Hex( file.getInputStream() );
        file.getInputStream().reset();
        return md5;
    }
}

