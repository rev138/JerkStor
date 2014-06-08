package org.hzsogood.jerkstor.controller;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSFile;
import org.hzsogood.jerkstor.config.SpringMongoConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Hashtable;

@Controller
public class FileUploadController {

    @RequestMapping(value="/fileupload", method=RequestMethod.GET)
    public @ResponseBody String provideUploadInfo() {
        return "You can upload a file by posting to this same URL.";
    }

    @RequestMapping(value = "/fileupload", method = RequestMethod.POST)
    @ResponseBody
    public String handleFileUpload(@RequestParam("name") String name, @RequestParam("file") MultipartFile file, @RequestParam("path") String filePath) {

        String fileName = "";

        if (!file.isEmpty()) {
            try {
                fileName = file.getOriginalFilename();

                ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
                GridFsOperations gridOperations = (GridFsOperations) ctx.getBean("gridFsTemplate");

                DBObject metaData = new BasicDBObject();
                metaData.put( "path", filePath );

                GridFSFile resultFile = gridOperations.store(file.getInputStream(), fileName, file.getContentType(), metaData);

                Hashtable result = new Hashtable();
                result.put( "id", resultFile.getId().toString() );

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

//import org.springframework.web.servlet.mvc.SimpleFormController;
//@RequestMapping( "/fileupload" )
//public class FileUploadController extends SimpleFormController{
//
//    public FileUploadController(){
//        setCommandClass(FileUpload.class);
//        setCommandName("fileUploadForm");
//    }
//
//
//    @Override
//    protected ModelAndView onSubmit(HttpServletRequest request,
//                                    HttpServletResponse response, Object command, BindException errors)
//            throws Exception {
//
//        FileUpload file = (FileUpload)command;
//
//        MultipartFile multipartFile = file.getFile();
//
//        String fileName="";
//
//        if(multipartFile!=null){
//            fileName = multipartFile.getOriginalFilename();
//            //do whatever you want
//
//            ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
//            GridFsOperations gridOperations = (GridFsOperations) ctx.getBean("gridFsTemplate");
//
//            DBObject metaData = new BasicDBObject();
//            metaData.put( "path", "/foo/placeholder");
//
//            gridOperations.store( multipartFile.getInputStream(), fileName, multipartFile.getContentType(), metaData);
//
//        }
//
//        return new ModelAndView("FileUploadSuccess","fileName",fileName);
//    }
//}