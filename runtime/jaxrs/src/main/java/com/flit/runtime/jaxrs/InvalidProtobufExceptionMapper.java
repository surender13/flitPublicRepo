package com.flit.runtime.jaxrs;

import com.flit.runtime.ErrorCode;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvalidProtobufExceptionMapper implements
    ExceptionMapper<InvalidProtocolBufferException> {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(InvalidProtobufExceptionMapper.class);

  @Context private HttpServletRequest request;

  @Override
  public Response toResponse(InvalidProtocolBufferException exception) {
    LOGGER.error("InvalidProtocolBufferException: request = {}, method = {}, msg= {}",
        request.getRequestURI(), request.getMethod(), exception.getMessage(), exception);

    Map<String, Object> response = new HashMap<>();
    response.put("code", ErrorCode.INVALID_ARGUMENT.getErrorCode());
    response.put("msg", exception.getMessage());
    return Response.status(ErrorCode.INVALID_ARGUMENT.getHttpStatus())
        .type(MediaType.APPLICATION_JSON)
        .entity(response)
        .build();
  }
}
