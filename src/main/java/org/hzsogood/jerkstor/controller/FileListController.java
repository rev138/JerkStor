package org.hzsogood.jerkstor.controller;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.util.JSON;
import org.hzsogood.jerkstor.service.GridFSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/file/list")
public class FileListController {
    @Autowired
    private GridFSService gridFSService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public String handleFileList( ModelMap model ) throws IOException {
        List<GridFSDBFile> gridFiles = gridFSService.find( new Query() );

        return JSON.serialize( gridFiles );
    }

}