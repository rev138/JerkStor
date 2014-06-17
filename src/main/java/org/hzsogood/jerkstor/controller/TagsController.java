package org.hzsogood.jerkstor.controller;

import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.hzsogood.jerkstor.service.GridFSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

@Controller
@RequestMapping(value = "/tags")
public class TagsController {
    @Autowired
    private GridFSService gridFSService;

    // add or remove tags from a file
    @ResponseBody
    @RequestMapping(value = "/{fileId}", method = RequestMethod.PUT)
    public Hashtable<String,Object> modifyTags(@PathVariable("fileId") String fileId, @RequestBody String tags, HttpServletResponse response ){
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        try {
            List<String> tagList = new ArrayList<String>();

            Collections.addAll(tagList, tags.split(","));

            for(String tag : tagList){
                if (tag.startsWith("-")) {
                    tag = tag.replaceAll("^-", "");
                    gridFSService.untagFile(fileId, tag);
                }
                else {
                    tag = tag.replaceAll("^+", "");
                    gridFSService.tagFile(fileId, tag);
                }

                GridFSDBFile file = gridFSService.findById(fileId);

                if(file == null){
                    result.put("error", "File not found");
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                else {
                    result.put("id", file.getId().toString());
                    result.put("tags", file.getMetaData().get("tags"));
                }
            }
        }
        catch (Exception e){
            result.put("error", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return result;
    }

    // clear all tags from a file
    @ResponseBody
    @RequestMapping(value = "/{fileId}", method = RequestMethod.DELETE)
    public Hashtable<String,Object> clearTags(@PathVariable("fileId") String fileId, HttpServletResponse response ){
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        try {
            GridFSDBFile file = gridFSService.findById(fileId);

            if(file == null){
                result.put("error", "File not found");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            else {
                DBObject metaData = file.getMetaData();

                metaData.put("tags", new ArrayList<String>());
                file.setMetaData(metaData);
                file.save();

                result.put("id", file.getId().toString());
                result.put("tags", file.getMetaData().get("tags"));
            }
        }
        catch (Exception e){
            result.put("error", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return result;
    }


    // find all files matching a comma-separated list of tags
    @ResponseBody
    @RequestMapping(value = "/{tags}", method = RequestMethod.GET)
    public Object findTags(@PathVariable("tags") String tags, HttpServletResponse response) {
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        try {
            List<String> tagList = new ArrayList<String>();

            Collections.addAll(tagList, tags.split(","));

            List<GridFSDBFile> files = gridFSService.findByAllTags(tagList);

            if (files == null || files.isEmpty()) {
                result.put("error", "File not found");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                List<Hashtable<String, Object>> fileList = new ArrayList<Hashtable<String, Object>>();
                for (GridFSDBFile g : files) {
                    fileList.add(gridFSService.getFileData(g));
                }
                return fileList;
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return result;
    }
}