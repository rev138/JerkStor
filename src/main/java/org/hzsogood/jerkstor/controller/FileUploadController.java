package org.hzsogood.jerkstor.controller;

import com.google.gson.Gson;
import org.hzsogood.jerkstor.service.GridFSServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Hashtable;

@Controller
public class FileUploadController {

    @RequestMapping(value="/file/upload", method=RequestMethod.GET)
    public @ResponseBody String provideUploadInfo() {
        return "You can upload a file by posting to this same URL.";
    }

    @RequestMapping(value = "/file/upload", method = RequestMethod.POST)
    @ResponseBody
    public String handleFileUpload(@RequestParam("name") String name, @RequestParam("file") MultipartFile file, @RequestParam("path") String filePath) {
        GridFSServiceImpl grid = new GridFSServiceImpl();

        if (!file.isEmpty()) {
            try {
                Hashtable<String, String> metaData = new Hashtable<String, String>();
                metaData.put( "path", filePath );

                String id = grid.store( file, metaData );

                Hashtable<String, String> result = new Hashtable<String, String>();
                result.put( "id", id );

                Gson gson = new Gson();

                return gson.toJson( result );

            } catch (Exception e) {
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name + " because the file was empty.";
        }
    }
}