package org.hzsogood.jerkstor.model;

import org.springframework.web.multipart.MultipartFile;

public class FileUpload {

    MultipartFile file;
    //getter and setter methods

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}