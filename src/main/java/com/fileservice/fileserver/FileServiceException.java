package com.fileservice.fileserver;

public class FileServiceException extends Exception{

    public enum FileServiceError {
        OPERATION_FAILED(1, "The requested operation could not be done."),
        FILE_NOT_FOUND(2, "File not found."),
        NO_READ_PERMISSION(3, "No read permission."),
        NO_WRITE_PERMISSION(4, "No write permission."),
        FILE_EXISTS(5, "File already exists"),
        UNAUTHORIZED(6, "User is unauthorized"),
        MANDATORY_PARAMETER_MISSING(7, "Mandatory parameter(s) missing");

        private final int code;
        private final String description;

        private FileServiceError(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return "{\"Error\":{\"Code\":" + this.getCode() + ",\"Description\":\"" + this.getDescription() + "\"}}";
        }
    }

    private final FileServiceError error;

    public FileServiceException(FileServiceError error) {
        super();
        this.error = error;
    }

    public String printJsonException() {
        return error.toString();
    }
}
