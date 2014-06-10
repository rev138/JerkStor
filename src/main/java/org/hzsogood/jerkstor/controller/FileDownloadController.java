package org.hzsogood.jerkstor.controller;

import com.mongodb.gridfs.GridFSDBFile;
import org.hzsogood.jerkstor.service.GridFSServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class FileDownloadController {

    @RequestMapping( value = "/file/download/{oid}", method = RequestMethod.GET)
    @ResponseBody
    public void handleFileDownload( @PathVariable("oid") String oid, HttpServletResponse response ) throws IOException {
        GridFSServiceImpl grid = new GridFSServiceImpl();
        GridFSDBFile gridFile = grid.findById( oid );

        response.setContentType( gridFile.getContentType() );
        response.setHeader( "Content-Disposition", "attachment; filename=" + gridFile.getFilename() );
        gridFile.writeTo( response.getOutputStream() );
        response.flushBuffer();
    }
}