package org.hzsogood.jerkstor.model;

import org.apache.commons.codec.binary.Base64;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

@Document(collection = "users")
public class User {

    @Id private
    String id;
    private String username;
    private String password;
    private String salt;
    private Date created;
    private boolean locked;

    public String getId() {
        return id;
    }

    public User(String username, String password) throws NoSuchAlgorithmException {
        this.username = username;
        this.setPassword(password);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Date getCreated() {
        return created;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String newPassword) throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        String salt = Base64.encodeBase64String(new BigInteger(128, random).toByteArray());
        MessageDigest md = MessageDigest.getInstance("SHA-512");

        md.update(newPassword.getBytes());
        md.update(salt.getBytes());

        this.password = Base64.encodeBase64String(md.digest());
        this.salt = salt;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean authenticate(String password) throws NoSuchAlgorithmException {
        if(this.locked){
            return false;
        }

        MessageDigest md = MessageDigest.getInstance("SHA-512");

        md.update(password.getBytes());
        md.update(this.salt.getBytes());

        return this.password.equals(Base64.encodeBase64String(md.digest()));
    }
}