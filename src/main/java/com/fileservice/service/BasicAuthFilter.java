package com.fileservice.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Base64;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fileservice.cache.DataCache;
import com.fileservice.fileserver.FileServiceException;
import com.fileservice.fileserver.FileServiceException.FileServiceError;

@PreMatching
public class BasicAuthFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    @Override
    public void filter(ContainerRequestContext rCtx) {
        try {
            rCtx.setSecurityContext(authenticate(rCtx));
        } catch (FileServiceException e) {
            abort(rCtx);
        } catch (NoSuchAlgorithmException e1) {
            LOGGER.error("Error occured while authenticating user");
            LOGGER.debug("Error : {}", e1);
            abort(rCtx);
        }
    }

    private class AuthContext implements SecurityContext {

        String login;
        String role;
        Principal principal;

        public AuthContext(String login, String role) {
            this.login = login;
            this.role = role;
            this.principal = new Principal() {

                @Override
                public String getName() {
                    return "file-service-users";
                }
            };
        }

        @Override
        public Principal getUserPrincipal() {
            return this.principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            return role.equals(this.role);
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public String getAuthenticationScheme() {
            return SecurityContext.BASIC_AUTH;
        }

        public String getLogin() {
            return login;
        }
    }

    private AuthContext authenticate(ContainerRequestContext rCtx) throws FileServiceException, NoSuchAlgorithmException {
        String authString = rCtx.getHeaderString(ContainerRequest.AUTHORIZATION);
        if(null == authString || !authString.startsWith("Basic ")) {
            LOGGER.debug("Invalid Auth String");
            throw new FileServiceException(FileServiceError.UNAUTHORIZED);
        }
        String[] credentials = new String(Base64.getDecoder().
                decode(authString.substring("Basic ".length()))).split(":");
        if(credentials.length < 2) {
            LOGGER.debug("Invalid Auth String");
            throw new FileServiceException(FileServiceError.UNAUTHORIZED);
        }
        String user = credentials[0];
        String passMd5;
        try {
            passMd5 = md5(credentials[1]);
        } catch (NoSuchAlgorithmException e) {
            throw e;
        }
        String role = DataCache.authenticate(user, passMd5);
        if(null == role)
            throw new FileServiceException(FileServiceError.UNAUTHORIZED);
        LOGGER.debug("User {} is authorized with role {}", user, role);
        return new AuthContext(user, role);
    }

    private void abort(ContainerRequestContext rCtx) {
        rCtx.abortWith(Response
                .status(Response.Status.UNAUTHORIZED)
                .build());
    }

    private String md5(String pass) throws NoSuchAlgorithmException {
        MessageDigest md;
        md = MessageDigest.getInstance("MD5");
        md.update(pass.getBytes());
        byte[] md5Bytes = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++)
            sb.append(Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1));
        return sb.toString();
    }
}
