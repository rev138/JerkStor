package org.hzsogood.jerkstor.aspect;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hzsogood.jerkstor.model.KeyPair;
import org.hzsogood.jerkstor.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Hashtable;

@Aspect
class AuthenticateRequest {
    @Autowired
    private MongoTemplate mongoOperations;

    @Around("execution(* org.hzsogood.jerkstor.controller.api..*(..)) && args(..,request,response)")
    public Object authenticateFilesMethods(ProceedingJoinPoint joinPoint, HttpServletRequest request, HttpServletResponse response) throws Throwable {
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        if (request.getHeader("authorization") == null || request.getHeader("timestamp") == null) {
            result.put("error", "Unauthorized: missing headers");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        else {
            String authHeader = request.getHeader("authorization");

            authHeader = authHeader.replaceAll("\\s+", "");

            String[] credentials = authHeader.split(":", 2);

            if(credentials.length == 2) {
                String key = credentials[0];
                String clientSignature = credentials[1];
                String secret = this.getSecret(key);
                String serverSignature = "";

                if(secret != null) {
                    serverSignature = this.signRequest(request, secret);
                }

//                System.out.println("client: " + clientSignature);
//                System.out.println("server: " + serverSignature);
//                System.out.println();

//                User user = new User("terd", "merdwfrgqrew");
//                mongoOperations.save(user);
//
//                KeyPair keyPair = new KeyPair(user.getId(), new Date(System.currentTimeMillis() + 7776000000L));
//                mongoOperations.save(keyPair);

                if (serverSignature != null && serverSignature.equals(clientSignature)) {
                    // allow the intercepted method to proceed
                    return joinPoint.proceed();
                }
                else{
                    result.put("error", "Unauthorized: invalid credentials");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return result;
                }
            }
            else {
                result.put("error", "Unauthorized: malformed Authorization header");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }

        return result;
    }

    String signRequest(HttpServletRequest request, String secret) throws NoSuchAlgorithmException {
        String data = request.getMethod() + " " + request.getRequestURI() + "\n"
            + request.getContentType() + "\n"
            + request.getHeader("timestamp") + "\n";

        String queryParams = (request.getQueryString());

        if(queryParams == null) {
            data += "";
        }
        else {
            data += this.parseQueryParams(queryParams);
        }

        return this.getHMAC(secret, data);
    }

    String parseQueryParams(String queryString) {
        if(queryString == null){
            return queryString;
        }
        else{
            queryString = queryString.toLowerCase();

            String[] pairs = queryString.split("&");

            Arrays.sort(pairs);

            return StringUtils.join(pairs, "&");
        }
    }

    String getHMAC(String secret, String data) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(),	"HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            return new String(Base64.encodeBase64(rawHmac));
        }
        catch (GeneralSecurityException e) {
            throw new IllegalArgumentException();
        }
    }

    String getSecret(String key) {
        KeyPair keyPair = mongoOperations.findOne(new Query(Criteria.where("key").is(key)), KeyPair.class);

        if(keyPair == null || keyPair.isLocked() || keyPair.isExpired()){
            return null;
        }

        User user = mongoOperations.findOne(new Query(Criteria.where("_id").is(keyPair.getOwnerId())), User.class);

        if(user == null || user.isLocked()){
            return null;
        }
        else {
            return keyPair.getSecret();
        }
    }
}