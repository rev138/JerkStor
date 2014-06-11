package org.hzsogood.jerkstor.controller;

import com.google.gson.Gson;
import org.hzsogood.jerkstor.service.GridFSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
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
    public String handleFileUpload(@RequestParam("name") String fileName, @RequestParam("file") MultipartFile file, @RequestParam("path") String filePath, String permissions, String tags ) {

        if (!file.isEmpty()) {
            // set default values
            if (fileName == null) {
                fileName = file.getOriginalFilename();
            }
            if (permissions == null) {
                permissions = "0644";
            }

            // strip leading/trailing slashes
            filePath = filePath.replaceAll( "(^/|^\\\\|/$|\\\\$)", "" );

            // add file metadata
            HashMap<String, Object> metaData = new HashMap<String, Object>();
            metaData.put( "filepermissions", permissions );
            metaData.put( "filename", fileName );
            metaData.put( "filepath", filePath );
            metaData.put( "tags", tags.split(","));

            try {
                String id = gridFSService.store( file, filePath + "/" + fileName , metaData );

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