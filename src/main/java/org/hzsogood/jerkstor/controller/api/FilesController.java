package org.hzsogood.jerkstor.controller.api;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.hzsogood.jerkstor.service.GridFSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping(value="/api/files")
public class FilesController {
    @Autowired
    private GridFSService gridFSService;

    // upload a file
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public Hashtable<String, Object> filesUploadFile(@RequestParam("name") String fileName, @RequestParam("file") MultipartFile file, @RequestParam("path") String filePath, @RequestParam("tags") String tags, HttpServletRequest request, HttpServletResponse response) {
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        if (!file.isEmpty()) {
            // set default values
            if (fileName == null) {
                fileName = file.getOriginalFilename();
            }

            // strip leading/trailing slashes
            filePath = filePath.replaceAll( "(^/+|^\\\\+|/+$|\\\\+$)", "" );
            // separate tags
            ArrayList<String> tagList = new ArrayList<String>(Arrays.asList(tags.split(",")));

            try {
                DBObject metaData = new BasicDBObject();
                // See if a file already exists with this name and path
                GridFSDBFile oldFile = gridFSService.findOne(fileName, filePath);

                // If it does, capture its tags so we can replace it with the new file
                if(oldFile != null) {
                    BasicDBList oldTags = (BasicDBList) oldFile.getMetaData().get("tags");
                    for(Object t: oldTags.toArray()){
                        if(!tagList.contains(t.toString()) && !t.toString().isEmpty()) {
                            tagList.add(t.toString());
                        }
                    }
                }

                // add file metadata
                metaData.put("path", filePath);
                metaData.put("tags", tagList);

                String id = gridFSService.store( file, fileName , metaData );

                // we added the new file successfully, so out with the old
                if(!id.isEmpty() && oldFile != null) {
                    gridFSService.deleteById(oldFile.get("_id").toString());
                }

                response.setStatus(HttpServletResponse.SC_CREATED);
                response.setHeader("Location", request.getRequestURL() + "/" + id);
                result.put("id", id);
            } catch (Exception e) { this.internalServerError(result, response, e); }

        } else {
            result.put("error", "You failed to upload " + fileName + " because the file was empty." );
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        return result;
    }

    // get all files' metadata
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public Object filesGetAllFiles(HttpServletRequest request, HttpServletResponse response) {
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        try {
            List<GridFSDBFile> files = gridFSService.find(new Query());

            if(files == null || files.isEmpty()){ this.fileNotFound(result, response); }
            else {
                List<Hashtable<String,Object>> fileList = new ArrayList<Hashtable<String,Object>>();
                for(GridFSDBFile g : files){
                    fileList.add(gridFSService.getFileData(g));
                }
                return fileList;
            }
        } catch (Exception e) { this.internalServerError(result, response, e); }

        return result;
    }

    // get one file's metadata
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Hashtable<String, Object> filesGetFile(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        try {
            GridFSDBFile file = gridFSService.findById(id);
            if(file == null){ this.fileNotFound(result, response); }
            else {
                result = gridFSService.getFileData(file);
            }
        } catch (Exception e){ this.internalServerError(result, response, e); }

        return result;
    }

    // delete a file
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Hashtable<String, Object> filesDeleteFile(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        try {
            GridFSDBFile file = gridFSService.findById(id);

            if (file == null) { this.fileNotFound(result, response); }
            else {
                result.put("id", id);
                gridFSService.deleteById(id);
            }
        } catch (Exception e) { this.internalServerError(result, response, e); }

        return result;
    }

    // rename a file
    @ResponseBody
    @RequestMapping(value = "/{id}/rename", method = RequestMethod.PUT)
    public Hashtable<String, Object> filesRenameFile(@PathVariable("id") String id, @RequestBody String name, HttpServletRequest request, HttpServletResponse response){
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        try {
            GridFSDBFile file = gridFSService.findById(id);

            if (file == null) { this.fileNotFound(result, response); }
            else {
                // see if a file with that name and path exists already
                GridFSDBFile checkFile = gridFSService.findOne(new Query(Criteria.where("filename").is(name).andOperator(Criteria.where("metadata.path").is(file.getMetaData().get("path")))));

                // if not, let's rename it
                if(checkFile == null) {
                    file.put("filename", name);
                    file.save();
                    result = gridFSService.getFileData(file);
                }
                else{
                    result.put("error", "a file named " + name + " already exists in " + file.getMetaData().get("path"));
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                }
            }
        } catch (IOException e) { this.internalServerError(result, response, e); }

        return result;
    }

    // move a file
    @ResponseBody
    @RequestMapping(value = "/{id}/move", method = RequestMethod.PUT)
    public Hashtable<String, Object> filesMoveFile(@PathVariable("id") String id, @RequestBody String path, HttpServletRequest request, HttpServletResponse response){
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        try {
            GridFSDBFile file = gridFSService.findById(id);

            if (file == null) { this.fileNotFound(result, response); }
            else {
                // see if a file with that name and path exists already
                GridFSDBFile checkFile = gridFSService.findOne(new Query(Criteria.where("filename").is(file.getFilename()).andOperator(Criteria.where("metadata.path").is(path))));

                // if not, let's move it
                if(checkFile == null) {
                    DBObject metaData = file.getMetaData();

                    metaData.put("path", path);
                    file.setMetaData(metaData);
                    file.save();

                    result = gridFSService.getFileData(file);
                }
                else{
                    result.put("error", "a file named " + file.getFilename() + " already exists in " + path);
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                }
            }
        } catch (IOException e) { this.internalServerError(result, response, e); }

        return result;
    }

    // copy a file
    @ResponseBody
    @RequestMapping(value = "/{id}/copy", method = RequestMethod.POST)
    public Hashtable<String, Object> filesCopyFile(@PathVariable("id") String id, @RequestParam("filename") String filename, @RequestParam("path") String path, HttpServletRequest request, HttpServletResponse response){
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        try {
            GridFSDBFile file = gridFSService.findById(id);

            if (file == null) { this.fileNotFound(result, response); }
            else {
                // see if a file with the new name and path exists already
                GridFSDBFile checkFile = gridFSService.findOne(new Query(Criteria.where("filename").is(filename).andOperator(Criteria.where("metadata.path").is(path))));

                // if not, let's copy it
                if(checkFile == null) {
                    DBObject metaData = file.getMetaData();

                    metaData.put("path", path);

                    String newId = gridFSService.store( file, filename, metaData);

                    result.put("id", newId);
                }
                else{
                    result.put("error", "a file named " + filename + " already exists in " + path);
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                }
            }
        } catch (IOException e) { this.internalServerError(result, response, e); }

        return result;
    }

    // add or remove tags from a file
    @ResponseBody
    @RequestMapping(value = "/{id}/tags", method = RequestMethod.PUT)
    public Hashtable<String,Object> filesModifyTags(@PathVariable("id") String id, @RequestBody String tags, HttpServletRequest request, HttpServletResponse response ){
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        try {
            List<String> tagList = new ArrayList<String>();

            Collections.addAll(tagList, tags.split(","));

            for(String tag : tagList){
                if (tag.startsWith("-")) {
                    tag = tag.replaceAll("^-", "");
                    gridFSService.untagFile(id, tag);
                }
                else {
                    tag = tag.replaceAll("^+", "");
                    gridFSService.tagFile(id, tag);
                }

                GridFSDBFile file = gridFSService.findById(id);

                if(file == null){ this.fileNotFound(result, response); }
                else {
                    result.put("id", file.getId().toString());
                    result.put("tags", file.getMetaData().get("tags"));
                }
            }
        } catch (Exception e){ this.internalServerError(result, response, e); }

        return result;
    }

    // clear all tags from a file
    @ResponseBody
    @RequestMapping(value = "/{id}/tags", method = RequestMethod.DELETE)
    public Hashtable<String,Object> filesClearTags(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response ){
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        try {
            GridFSDBFile file = gridFSService.findById(id);

            if(file == null){ this.fileNotFound(result, response); }
            else {
                DBObject metaData = file.getMetaData();

                metaData.put("tags", new ArrayList<String>());
                file.setMetaData(metaData);
                file.save();

                result.put("id", file.getId().toString());
                result.put("tags", file.getMetaData().get("tags"));
            }
        } catch (Exception e){ this.internalServerError(result, response, e); }

        return result;
    }

    // find all files matching a comma-separated list of tags
    @ResponseBody
    @RequestMapping(value = "/tags/{tags}", method = RequestMethod.GET)
    public Object filesFindTags(@PathVariable("tags") String tags, HttpServletResponse response) {
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        try {
            List<String> tagList = new ArrayList<String>();

            Collections.addAll(tagList, tags.split(","));

            List<GridFSDBFile> files = gridFSService.findByAllTags(tagList);

            if (files == null || files.isEmpty()) {
                files = new ArrayList<GridFSDBFile>();
            } else {
                List<Hashtable<String, Object>> fileList = new ArrayList<Hashtable<String, Object>>();

                for (GridFSDBFile g : files) {
                    fileList.add(gridFSService.getFileData(g));
                }

                return fileList;
            }
        } catch (Exception e) { this.internalServerError(result, response, e); }

        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/path/{path}", method = RequestMethod.GET)
    public Object filesFindByPath(@PathVariable("path") String path, HttpServletRequest request, HttpServletResponse response) {
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        // forward-slashes in the url are parsed as path parts, so we need to use pipes in the file path, then convert
        // them back to forward-slashes inside the controller
        path = path.replaceAll( "\\|", "/" );

        try {
            List<GridFSDBFile> files = gridFSService.findByPath(path);

            if(files == null || files.isEmpty()){
                files = new ArrayList<GridFSDBFile>();
            }
            else{
                List<Hashtable<String, Object>> fileList = new ArrayList<Hashtable<String, Object>>();

                for (GridFSDBFile g : files) {
                    fileList.add(gridFSService.getFileData(g));
                }
                return fileList;
            }
        } catch (Exception e) { this.internalServerError(result, response, e); }

        return result;
    }

    // download a file
    @ResponseBody
    @RequestMapping(value="/{id}/download", method=RequestMethod.GET)
    public Hashtable<String, Object> filesDownloadFile(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        try {
            GridFSDBFile file = gridFSService.findById(id);

            if(file == null){ this.fileNotFound(result, response); }
            else {
                response.setContentType(file.getContentType());
                response.setHeader("Content-Disposition", "attachment; filename=" + file.getFilename());
                file.writeTo(response.getOutputStream());
                response.flushBuffer();
            }
        } catch (Exception e) { this.internalServerError(result, response, e); }

        return result;
    }

    void internalServerError (Hashtable<String, Object> result, HttpServletResponse response, Exception e) {
        result.put("error", e.getMessage());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    void fileNotFound (Hashtable<String, Object> result, HttpServletResponse response) {
        result.put("error", "File not found");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}