package org.hzsogood.jerkstor.controller;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.hzsogood.jerkstor.config.SpringMongoConfig;
import org.hzsogood.jerkstor.model.FileUpload;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class FileUploadController extends SimpleFormController{

    public FileUploadController(){
        setCommandClass(FileUpload.class);
        setCommandName("fileUploadForm");
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request,
                                    HttpServletResponse response, Object command, BindException errors)
            throws Exception {

        FileUpload file = (FileUpload)command;

        MultipartFile multipartFile = file.getFile();

        String fileName="";

        if(multipartFile!=null){
            fileName = multipartFile.getOriginalFilename();
            //do whatever you want

            ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
            GridFsOperations gridOperations = (GridFsOperations) ctx.getBean("gridFsTemplate");

            DBObject metaData = new BasicDBObject();

            gridOperations.store( multipartFile.getInputStream(), multipartFile.getOriginalFilename(), multipartFile.getContentType(), metaData);

        }

        return new ModelAndView("FileUploadSuccess","fileName",fileName);
    }
}