package com.vdp.spring.bootstrap.rest;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdp.spring.exception.AuthenticationException;
import com.vdp.spring.exception.AuthorizationException;
import com.vdp.spring.exception.BusinessException;
import com.vdp.spring.exception.InvalidArgumentsException;
import com.vdp.spring.exception.MaximumRequestsLimitException;
import com.vdp.spring.exception.ObjectAlreadyExistsException;
import com.vdp.spring.exception.ObjectNotFoundException;
import com.vdp.spring.exception.ObjectStateChangedSinceLastRequestException;

/**
 * Custom Rest/Json Exception handler for returning JSON responses when Exceptions are uncaught.
 */
public class RestJsonExceptionResolver extends AbstractHandlerExceptionResolver implements View {

    private static final Logger LOG = LoggerFactory.getLogger(RestJsonExceptionResolver.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String KEY_ERROR_OBJECT = "error";
    private static final Map<String, Integer> httpErrorCodeMap = new HashMap<>();
    /**
     * Show a custom message instead of the one coming from the exception.
     * If empty or null the message from the exception will be used.
     * Default behavior will be to populate the message from the exception.
     */
    private static String customMsg;
    private ErrorIdGenerator errorIdGenerator = new DefaultErrorIdGenerator();

    /**
     * Disable technical diagnostics like stack traces.
     * Default value is FALSE
     */
    private boolean diagnosticsDisabled;

    static {
        httpErrorCodeMap.put(InvalidArgumentsException.class.getName(), 400);
        httpErrorCodeMap.put(AuthenticationException.class.getName(), 401);
        httpErrorCodeMap.put(AuthorizationException.class.getName(), 403);
        httpErrorCodeMap.put(ObjectNotFoundException.class.getName(), 404);
        httpErrorCodeMap.put(ObjectAlreadyExistsException.class.getName(), 409);
        httpErrorCodeMap.put(ObjectStateChangedSinceLastRequestException.class.getName(), 409);
        httpErrorCodeMap.put(MaximumRequestsLimitException.class.getName(), 429);
        httpErrorCodeMap.put(BusinessException.class.getName(), 500);
    }

    @SuppressWarnings("deprecation")
    public RestJsonExceptionResolver() {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception e) {
        ModelAndView mav = new ModelAndView(this);
        RestError error = new RestError();

        if (e != null) {
            error.setMessageOriginal(e.getLocalizedMessage());
            if (StringUtils.isEmpty(customMsg)) {
                // Every JRestError will have a message
                error.setMessage(e.getLocalizedMessage());
            } else {
                error.setMessage(customMsg);
            }

            // Only BusinessExceptions have possible errorCodes
            if (e instanceof BusinessException) {
                error.setErrorCode(((BusinessException) e).getErrorCode());
            } else if (e instanceof AuthenticationException && ((AuthenticationException) e).getErrorCode() != 0) {
                // Differentiate errorCode when login failed, invalid-account, or not active, this can be set base on project
                error.setErrorCode(((AuthenticationException) e).getErrorCode());
            }

            // Set HTTP error code. If we don't have this Exception type registered, then we will provide a
            // diagnostic and error id.
            Integer httpCode = httpErrorCodeMap.get(e.getClass().getName());
            if (httpCode == null) {
                httpCode = 500;

                StackTraceElement[] trace = null;
                StringBuilder sb = new StringBuilder();

                if (e.getCause() == null) {
                    trace = e.getStackTrace();
                    sb.append("Exception - ").append(e.getClass().getName());
                    sb.append("\r\nStack -\r\n");
                } else {
                    trace = e.getCause().getStackTrace();
                    sb.append("\r\nException Cause -\r\n");
                }

                for (int i = 0; i < trace.length; i++) {
                    sb.append("\r\n\tat ").append(trace[i].toString());
                }
                error.setDiagnosticOriginal(sb.toString());

                if (!diagnosticsDisabled) {
                    error.setDiagnostic(sb.toString());
                }

                error.setErrorId(errorIdGenerator.generateId());

            }
            // check if errorCode is specified, take original message do not override
            if (error.getErrorCode() != null && error.getErrorCode() != 0) {
                error.setMessage(e.getLocalizedMessage());
            }
            error.setHttpCode(httpCode);
        } else {
            error.setMessage("Unknown error");
            error.setHttpCode(500);
            error.setErrorId(errorIdGenerator.generateId());
        }

        mav.addObject(KEY_ERROR_OBJECT, error);
        LOG.error("Error {}", error);
        return mav;
    }

    /**
     * @see View#render
     */
    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RestError e = (RestError) model.get(KEY_ERROR_OBJECT);

        response.setContentType(getContentType());
        response.setStatus(e.getHttpCode());

        MAPPER.writeValue(response.getOutputStream(), e);
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    /**
     * Set a custom Error Id Generator
     *
     * @param errorIdGenerator The Error Id Generator to use
     */
    public void setErrorIdGenerator(ErrorIdGenerator errorIdGenerator) {
        this.errorIdGenerator = errorIdGenerator;
    }

    /**
     * Register a specific Exception to always return the supplied HTTP response code.
     *
     * @param e Exception to register
     * @param httpResponseCode The HTTP response code to send back to the client for this given Exception
     */
    public static void registerExceptionWithHTTPCode(Exception e, int httpResponseCode) {
        httpErrorCodeMap.put(e.getClass().getName(), httpResponseCode);
    }

    public static void registerExceptionWithHTTPCode(Class<? extends Exception> clazz, int httpResponseCode) {
        httpErrorCodeMap.put(clazz.getName(), httpResponseCode);
    }

    public void setDiagnosticsDisabled(boolean diagnosticsDisabled) {
        this.diagnosticsDisabled = diagnosticsDisabled;
    }

    public static void setCustomMsg(String customMsg) {
        RestJsonExceptionResolver.customMsg = customMsg;
    }

    private static final class RestError {

        private int httpCode;
        private Integer errorCode;
        private String message;
        private String errorId;
        private String diagnostic;
        @JsonIgnore
        private String messageOriginal;
        @JsonIgnore
        private String diagnosticOriginal;

        public int getHttpCode() {
            return httpCode;
        }

        public void setHttpCode(int httpCode) {
            this.httpCode = httpCode;
        }

        public Integer getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getErrorId() {
            return errorId;
        }

        public void setErrorId(String errorId) {
            this.errorId = errorId;
        }

        public String getDiagnostic() {
            return diagnostic;
        }

        public void setDiagnostic(String diagnostic) {
            this.diagnostic = diagnostic;
        }

        @Override
        public String toString() {
            return String.format("HTTP %d {errorCode:%d, message:%s, errorId:%s, diagnostic:%s}", httpCode, errorCode,
                    messageOriginal, errorId, diagnosticOriginal);
        }

        /**
         * @param messageOriginal the messageOriginal to set
         */
        public void setMessageOriginal(String messageOriginal) {
            this.messageOriginal = messageOriginal;
        }

        /**
         * @param diagnosticOriginal the diagnosticOriginal to set
         */
        public void setDiagnosticOriginal(String diagnosticOriginal) {
            this.diagnosticOriginal = diagnosticOriginal;
        }
    }

}
