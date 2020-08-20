package com.flit.runtime.jaxrs;

import com.flit.runtime.FlitException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class FlitExceptionMapper implements ExceptionMapper<FlitException> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlitExceptionMapper.class);

  @Context private HttpServletRequest request;

  @Override
  public Response toResponse(FlitException exception) {
    LOGGER.error("Flit exception: request = {}, method = {}, code = {}, msg = {}",
        request.getRequestURI(), request.getMethod(), exception.getErrorCode(),
        exception.getMessage(), exception);

    Map<String, Object> response = new HashMap<>();
    response.put("code", exception.getErrorCode().getErrorCode());
    response.put("msg", exception.getMessage());

    if (exception.hasMeta()) {
      response.put("meta", exception.getMeta());
    }
    return Response.status(exception.getErrorCode().getHttpStatus())
        .type(MediaType.APPLICATION_JSON)
        .entity(response)
        .build();
  }
}
