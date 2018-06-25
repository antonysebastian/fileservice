package com.fileservice.service;

import java.io.InputStream;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fileservice.fileserver.FileServiceException;
import com.fileservice.fileserver.FileServiceException.FileServiceError;

/**
 * Root resource (exposed at "files" path)
 */
@Path("fileService")
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String ACCEPT_RANGES = "Accept-Ranges";
    private static final String BYTES_TYPE = "BYTES";
    private static final String FILE_CONTENT_DISPOSITION = "attachment; filename=%s";
    private static final String SUCCESS_RESPONSE = "{\"OK\":{\"Operation\":\"%s\",\"Resource\":\"%s\"}}";


    @GET
    @Path("/files/{fileName}")
    @Produces( {MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON} )
    @PermitAll
    public Response download(@PathParam("fileName") String fileName) {
        LOGGER.debug("Received GET request for file {}", fileName);
        try {
            byte[] file = FileUtil.download(fileName);
            if(null != file) {
                ResponseBuilder response = Response.ok(file);
                response.header(CONTENT_DISPOSITION, String.format(FILE_CONTENT_DISPOSITION, fileName));
                response.header(CONTENT_LENGTH, file.length);
                response.header(ACCEPT_RANGES, BYTES_TYPE);
                LOGGER.debug("File {} returned successfully", fileName);
                return response.build();
            }
        } catch (FileServiceException e) {
            LOGGER.error("Could not return file {}", fileName);
            return Response.ok(e.printJsonException(), MediaType.APPLICATION_JSON).build();
        }
        LOGGER.error("Could not return file {}", fileName);
        return Response.ok(new FileServiceException(FileServiceError.OPERATION_FAILED).printJsonException(),
                MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/files")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public Response upload(@FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition metadata) {
        if(null == fileInputStream || null == metadata || null == metadata.getFileName()) {
            LOGGER.debug("Mandatory parameter file missing");
            return Response.status(Status.BAD_REQUEST)
                    .entity(new FileServiceException(FileServiceError.MANDATORY_PARAMETER_MISSING).printJsonException())
                    .build();
        }
        LOGGER.debug("Received POST request for file {}", metadata.getFileName());
        String fileName = metadata.getFileName();
        try {
            if(!FileUtil.upload(fileInputStream, fileName)) {
                LOGGER.error("File {} could not be written", fileName);
                return Response.ok(new FileServiceException(FileServiceError.OPERATION_FAILED).printJsonException(),
                        MediaType.APPLICATION_JSON).build();
            }
        } catch (FileServiceException e) {
            LOGGER.error("File {} could not be written", fileName);
            return Response.ok(e.printJsonException(), MediaType.APPLICATION_JSON).build();
        }
        LOGGER.debug("File {} written successsfully", fileName);
        return Response.ok(String.format(SUCCESS_RESPONSE, "WRITE", fileName)).build();
    }

    @RolesAllowed("admin")
    @DELETE
    @Path("/files/{fileName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("fileName") String fileName) {
        LOGGER.debug("Received DELETE request for file {}", fileName);
        try {
            if(!FileUtil.delete(fileName)) {
                LOGGER.error("File {} could not be deleted", fileName);
                return Response.ok(new FileServiceException(FileServiceError.OPERATION_FAILED).printJsonException(),
                        MediaType.APPLICATION_JSON).build();
            }
        } catch (FileServiceException e) {
            LOGGER.error("File {} could not be deleted", fileName);
            return Response.ok(e.printJsonException(), MediaType.APPLICATION_JSON).build();
        }
        LOGGER.debug("File {} deleted successsfully", fileName);
        return Response.ok(String.format(SUCCESS_RESPONSE, "DELETE", fileName)).build();
    }

}