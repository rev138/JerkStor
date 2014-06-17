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
public class FilesListController {
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

    @RequestMapping(value = "/file/list/path/{path}", method = RequestMethod.GET)
    @ResponseBody
    public String handleFileListPath(@PathVariable("path") String path) throws IOException {
        // forward-slashes in the url are parsed as path parts, so we need to use pipes in the file path, then convert
        // them back to forward-slashes inside the controller
        path = path.replaceAll( "\\|", "/" );
        return JSON.serialize(gridFSService.findByPathRecursive(path));
    }
}

