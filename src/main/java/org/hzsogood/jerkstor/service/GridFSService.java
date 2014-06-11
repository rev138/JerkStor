package org.hzsogood.jerkstor.service;

import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public interface GridFSService {
    String store ( MultipartFile file, String name, HashMap metaData ) throws IOException;

    String store ( MultipartFile file, HashMap metaData ) throws IOException;

    String store ( MultipartFile file, String name ) throws IOException;

    String store ( MultipartFile file ) throws IOException;

    List<GridFSDBFile> find ( Query query ) throws IOException;

    GridFSDBFile findOne ( Query query ) throws IOException;

    GridFSDBFile findById ( String oid ) throws IOException;

    List<GridFSDBFile> findByTag ( String tag ) throws IOException;

    List<GridFSDBFile> findByAllTags ( List<String> tags ) throws IOException;

    List<GridFSDBFile> findByAnyTags ( List<String> tags ) throws IOException;
}