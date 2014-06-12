package org.hzsogood.jerkstor.controller;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.util.JSON;
import org.hzsogood.jerkstor.service.GridFSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Hashtable;

@Controller
public class FileDeleteController {
    @Autowired
    private GridFSService gridFSService;

    @RequestMapping(value = "/file/delete/{oid}", method = RequestMethod.GET)
    @ResponseBody
    public String handleFileDelete(@PathVariable("oid") String oid) throws IOException {
        Hashtable<String, String> result = new Hashtable<String, String>();

        GridFSDBFile file = gridFSService.findById(oid);

        if( file == null ){
            result.put("ok", "0");
            result.put("message", "the file does not exist");
        }
        else {
            try {
                gridFSService.deleteById(oid);
                result.put("ok", "1");
            }
            catch (Exception e) {
                result.put("ok", "0");
                result.put("message", e.getMessage());
            }
        }

        return JSON.serialize(result);
    }

}
