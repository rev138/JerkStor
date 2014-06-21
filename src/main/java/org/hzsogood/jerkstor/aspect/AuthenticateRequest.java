package org.hzsogood.jerkstor.aspect;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

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
    @Around("execution(* org.hzsogood.jerkstor.controller.FilesController.files*(..)) && args(..,request,response)")
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
                String serverSignature = this.signRequest(request, this.getKeySecret(key));

//                System.out.println("client: " + clientSignature);
//                System.out.println("server: " + serverSignature);
//                System.out.println();

                if (serverSignature.equals(clientSignature)) {
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
                result.put("error", "Unauthorized: invalid credentials");
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

//        System.out.println("***");
//        System.out.println(data);
//        System.out.println("***");

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

//    String getMD5(String content) throws NoSuchAlgorithmException {
//        MessageDigest digest = MessageDigest.getInstance("MD5");
//
//        digest.update(content.getBytes());
//
//        return new String(Base64.encodeBase64(digest.digest()));
//    }

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

    String getKeySecret(String key) {
        return "foo";
    }
}