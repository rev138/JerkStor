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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

@Controller
public class FileDeleteController {
    @Autowired
    private GridFSService gridFSService;

    @RequestMapping(value = "/file/delete/id/{oids}", method = RequestMethod.GET)
    @ResponseBody
    public String handleFileDeleteByIds(@PathVariable("oids") String oids) throws IOException {
        Hashtable<String, String> result = new Hashtable<String, String>();

        List<String> oidList = new ArrayList<String>();
        Collections.addAll(oidList, oids.split(","));

        try {
            for (String oid : oidList) {
                GridFSDBFile file = gridFSService.findById(oid);

                if( file != null ){
                    gridFSService.deleteById(oid);
                }
            }
            result.put("ok", "1");
        }
        catch (Exception e) {
            result.put("ok", "0");
            result.put("message", e.getMessage());
        }

        return JSON.serialize(result);
    }
}
