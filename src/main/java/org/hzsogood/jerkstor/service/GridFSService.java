package org.hzsogood.jerkstor.service;

import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

public interface GridFSService {
    String store ( MultipartFile file, String name, DBObject metaData ) throws IOException;

    String store ( MultipartFile file, DBObject metaData ) throws IOException;

    String store ( MultipartFile file, String name ) throws IOException;

    String store ( MultipartFile file ) throws IOException;

    List<GridFSDBFile> find ( Query query ) throws IOException;

    GridFSDBFile findOne ( Query query ) throws IOException;

    GridFSDBFile findOne (String name, String path) throws IOException;

    GridFSDBFile findById ( String oid ) throws IOException;

    List<GridFSDBFile> findByTag ( String tag ) throws IOException;

    List<GridFSDBFile> findByAllTags ( List<String> tags ) throws IOException;

    List<GridFSDBFile> findByAnyTags ( List<String> tags ) throws IOException;

    List<GridFSDBFile> findByPath( String path ) throws IOException;

    List<GridFSDBFile> findByPathRecursive (String path) throws IOException;

    void delete ( Query query ) throws IOException;

    void deleteById ( String oid ) throws IOException;

    void tagFile ( String oid, String tag ) throws IOException;

    void untagFile ( String oid, String tag ) throws IOException;

    void setPath(String oid, String path) throws IOException;

    void setName(String oid, String name) throws IOException;

    Hashtable<String, Object> getFileData(GridFSDBFile file);
}