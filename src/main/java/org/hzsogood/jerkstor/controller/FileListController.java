package org.hzsogood.jerkstor.controller;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.util.JSON;
import org.hzsogood.jerkstor.service.GridFSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class FileListController {
    @Autowired
    private GridFSService gridFSService;

    @RequestMapping(value = "/file/list", method = RequestMethod.GET)
    @ResponseBody
    public String handleFileList() throws IOException {
        List<GridFSDBFile> gridFiles = gridFSService.find( new Query() );

        return JSON.serialize( gridFiles );
    }

    @RequestMapping(value = "/file/list/tags/{tags}", method = RequestMethod.GET)
    @ResponseBody
    public String handleFileListTags(@PathVariable("tags") String tags) throws IOException {
        List<String> tagList = new ArrayList<String>();

        Collections.addAll(tagList, tags.split(","));

        List<GridFSDBFile> gridFiles = gridFSService.findByAllTags( tagList );

        return JSON.serialize( gridFiles );
    }

    @RequestMapping(value = "/file/list/path", method = RequestMethod.POST)
    @ResponseBody
    public String handleFileListPath(@RequestParam("path") String path) throws IOException {
        return JSON.serialize(gridFSService.findByPath(path));
    }
}

