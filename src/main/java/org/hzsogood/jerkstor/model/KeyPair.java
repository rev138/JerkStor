package org.hzsogood.jerkstor.model;

import org.apache.commons.codec.binary.Base64;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Date;

@Document(collection = "key_pairs")
public class KeyPair {
    @Id private String key;
    private String ownerId;
    private String secret;
    private Date created;
    private Date expires;
    private boolean locked;

    public KeyPair(String ownerId, Date expires) throws Exception {
        try {
            this.ownerId = ownerId;
            this.key = this.generateHash("SHA-256");
            this.secret = this.generateHash("SHA-512");
            this.created = new Date();
            this.setExpires(expires);
        }
        catch (Exception e){
            throw e;
        }
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getKey() {
        return key;
    }

    public String getSecret() {
        return secret;
    }

    public Date getCreated() {
        return created;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isExpired() {
        return(new Date().after(expires));
    }

    String generateHash(String algorithm) throws Exception {
        try {
            SecureRandom random = new SecureRandom();
            String randomString = Base64.encodeBase64String(new BigInteger(256, random).toByteArray());
            MessageDigest md = MessageDigest.getInstance(algorithm);

            md.update(randomString.getBytes());

            return Base64.encodeBase64String(md.digest());
        }
        catch (Exception e){
            throw e;
        }
    }
}

