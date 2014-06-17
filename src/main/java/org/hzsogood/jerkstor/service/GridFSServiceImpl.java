package org.hzsogood.jerkstor.service;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

@Service
public final class GridFSServiceImpl implements GridFSService {
//    @Autowired
//    private ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
//    private GridFsOperations gridTemplate = (GridFsOperations) ctx.getBean("gridFsTemplate");
    
    @Autowired
    private GridFsTemplate gridTemplate;

    // store a file with a custom name and metadata
    @Override
    @Transactional
    public String store(MultipartFile file, String name, DBObject metaData) throws IOException {
        GridFSFile resultFile = gridTemplate.store(file.getInputStream(), name, file.getContentType(), metaData);

        return resultFile.getId().toString();
    }

    // store a file with the original name and metadata
    @Override
    public String store(MultipartFile file, DBObject metaData) throws IOException {
        String name = file.getOriginalFilename();

        return this.store(file, name, metaData);
    }

    // store a file with a custom name and no metadata
    @Override
    public String store(MultipartFile file, String name) throws IOException {
        DBObject metaData = new BasicDBObject();

        return this.store(file, name, metaData);
    }

    // store a file with the original name and no metadata
    @Override
    public String store(MultipartFile file) throws IOException {
        DBObject metaData = new BasicDBObject();
        String name = file.getOriginalFilename();

        return this.store(file, name, metaData);
    }

    // retrieve a list of file objects
    @Override
    @Transactional(readOnly = true)
    public List<GridFSDBFile> find(Query query) throws IOException {
        return gridTemplate.find( query );
    }

    // retrieve one file using an arbitrary query
    @Override
    @Transactional(readOnly = true)
    public GridFSDBFile findOne(Query query) throws IOException {
        return gridTemplate.findOne( query );
    }

    // retrieve one file using an arbitrary query
    @Override
    @Transactional(readOnly = true)
    public GridFSDBFile findOne(String name, String path) throws IOException {
        return this.findOne(new Query(Criteria.where("filename").is(name).and("metadata.path").is(path)));
    }

    // retrieve one file by OID
    @Override
    @Transactional(readOnly = true)
    public GridFSDBFile findById( String oid ) throws IOException {
        return gridTemplate.findOne( new Query().addCriteria( Criteria.where("_id").is( oid ) ) );
    }

    // retrieve a list of files by tag
    @Transactional(readOnly = true)
    public List<GridFSDBFile> findByTag ( String tag ) throws IOException {
        return this.find(new Query(Criteria.where("metadata.tags").is(tag)));
    }

    // retrieve a list of files by multiple tags
    @Transactional(readOnly = true)
    public List<GridFSDBFile> findByAllTags ( List<String> tags ) throws IOException {
        return this.find(new Query( Criteria.where("metadata.tags").all(tags)));
    }

    // retrieve a list of files by multiple tags
    @Transactional(readOnly = true)
    public List<GridFSDBFile> findByAnyTags ( List<String> tags ) throws IOException {
        return this.find(new Query( Criteria.where("metadata.tags").in(tags)));
    }

    @Transactional(readOnly = true)
    public List<GridFSDBFile> findByPath(String path) throws IOException {
        return this.find( new Query( Criteria.where("metadata.path").is(path)));
    }

    @Override
    public List<GridFSDBFile> findByPathRecursive(String path) throws IOException {
        path = path.replaceAll("/", "\\\\/");
        DBObject regex = new BasicDBObject("$regex", "^" + path + "/");
        return this.find( new Query(Criteria.where("metadata.path").is(path).orOperator(Criteria.where("metadata.path").is(regex.toString()))));
    }

    @Override
    @Transactional
    public void delete(Query query) throws IOException {
        gridTemplate.delete( query );
    }

    @Override
    @Transactional
    public void deleteById(String oid) throws IOException {
        this.delete(new Query().addCriteria( Criteria.where("_id").is( oid ) ));
    }

    @Transactional
    public void tagFile(String oid, String tag) throws IOException {
        GridFSDBFile file = this.findById(oid);
        DBObject metaData = file.getMetaData();

        BasicDBList tags = (BasicDBList) metaData.get("tags");

        // only add this tag if it's not already there
        if(!tags.contains(tag)){
            tags.add(tag);
            metaData.put("tags", tags);
            file.setMetaData(metaData);
            file.save();
        }
    }

    @Transactional
    public void untagFile(String oid, String tag) throws IOException {
        GridFSDBFile file = this.findById(oid);
        DBObject metaData = file.getMetaData();

        BasicDBList tags = (BasicDBList) metaData.get("tags");

        if(tags.contains(tag)){
            tags.remove(tag);
            metaData.put("tags", tags);
            file.setMetaData(metaData);
            file.save();
       }

    }

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

    @Transactional
    public void setName(String oid, String name) throws IOException {
        GridFSDBFile file = this.findById(oid);

        file.put("filename", name);
        file.save();
    }

    public Hashtable<String, Object> getFileData(GridFSDBFile file) {
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        result.put("id", file.getId().toString());
        result.put("filename", file.getFilename());
        result.put("path", file.getMetaData().get("path"));
        result.put("content-type", file.getContentType());
        result.put("bytes", file.getLength());
        result.put("tags", file.getMetaData().get("tags"));
        result.put("date", file.getUploadDate().toString());
        result.put("epoch", file.getUploadDate().getTime());
        result.put("md5", file.getMD5());

        return result;
    }

    // check if a file with this name and path already exists
    @Transactional(readOnly = true)
    boolean fileExists ( String fileName, String path ) throws IOException {
        GridFSDBFile file = gridTemplate.findOne(new Query().addCriteria(Criteria.where("filename").is(fileName)).addCriteria(Criteria.where("metadata.path").is(path)));
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

