package org.hzsogood.jerkstor.controller;

import com.mongodb.gridfs.GridFSDBFile;
import org.hzsogood.jerkstor.service.GridFSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class FileDownloadController {
    @Autowired
    private GridFSService gridFSService;

    @RequestMapping( value = "/file/download/{oid}", method = RequestMethod.GET)
    @ResponseBody
    public String handleFileDownload( @PathVariable("oid") String oid, HttpServletResponse response ) throws IOException {
        try {
            GridFSDBFile gridFile = gridFSService.findById(oid);

            response.setContentType(gridFile.getContentType());
            response.setHeader("Content-Disposition", "attachment; filename=" + gridFile.getFilename());
            gridFile.writeTo(response.getOutputStream());
            response.flushBuffer();
        }
        catch (Exception e) {
            return e.getMessage();
        }
        return "Foo";
    }
}