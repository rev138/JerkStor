package org.hzsogood.jerkstor.controller;

import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.hzsogood.jerkstor.service.GridFSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

@Controller
public class FileUploadController {
    @Autowired
    private GridFSService gridFSService;

    @RequestMapping(value="/file/upload", method=RequestMethod.GET)
    public @ResponseBody String provideUploadInfo() {
        return "You can upload a file by posting to this same URL.";
    }

    @RequestMapping(value = "/file/upload", method = RequestMethod.POST)
    @ResponseBody
    public String handleFileUpload(@RequestParam("name") String fileName, @RequestParam("file") MultipartFile file, @RequestParam("path") String filePath, @RequestParam("tags") String tags ) {

        if (!file.isEmpty()) {
            // set default values
            if (fileName == null) {
                fileName = file.getOriginalFilename();
            }
            // strip leading/trailing slashes
            filePath = filePath.replaceAll( "(^/+|^\\\\+|/+$|\\\\+$)", "" );

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

                Hashtable<String, String> result = new Hashtable<String, String>();
                result.put( "id", id );

                Gson gson = new Gson();

                return gson.toJson( result );
            }
            catch (Exception e) {
                return "You failed to upload " + fileName + " => " + e.getMessage();
            }

        } else {
            return "You failed to upload " + fileName + " because the file was empty.";
        }
    }
}