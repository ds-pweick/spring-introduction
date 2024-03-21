package de.doubleslash.spring.introduction.controller;

public abstract class ControllerConfiguration {
    public final static String ADD_CAR_SUCCESS_STRING = "Successfully added car %s %s";
    public final static String CAR_JSON_PARSE_FAILURE_STRING = "Car data sent is no valid JSON";
    public final static String MODEL_OR_BRAND_INVALID_STRING = "Car model and/or brand name invalid";
    public final static String FILE_UPLOAD_INVALID_NAME_FAILURE_STRING = "Name of file requested for upload is invalid," +
            " too long, or contains an extension which is prohibited.";
    public final static String FILE_UPLOAD_INTERNAL_ERROR_FAILURE_STRING = "File upload failed due to internal error.";
    public final static String CARS_BUCKET = "car-images";
    public static final String ENDPOINT_RECEIVED_INVALID_JSON = "Endpoint %s received invalid JSON";
    public final static String CARS_ROOT = "/cars";
    public final static String IMAGES_ROOT = "/images";
    public static String CAR_NOT_FOUND_STRING = "No car with requested id %d found";
    public static String REPLACE_CAR_SUCCESS_STRING = "Replacement successful";
    public static String DELETE_CAR_SUCCESS_STRING = "Deletion successful";
    public static String DELETE_CAR_BY_BRAND_SUCCESS_STRING = "Successfully deleted %d car(s) of brand %s";
    public static String DELETE_CAR_BY_BRAND_NONE_DELETED_NEUTRAL_STRING = "No cars were deleted";
}
