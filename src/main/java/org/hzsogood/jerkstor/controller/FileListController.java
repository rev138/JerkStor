package org.hzsogood.jerkstor.controller;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.util.JSON;
import org.hzsogood.jerkstor.service.GridFSServiceImpl;
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

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public String handleFileList( ModelMap model ) throws IOException {
        GridFSServiceImpl grid = new GridFSServiceImpl();

        List<GridFSDBFile> gridFiles = grid.find( new Query() );

        return JSON.serialize( gridFiles );
    }

}