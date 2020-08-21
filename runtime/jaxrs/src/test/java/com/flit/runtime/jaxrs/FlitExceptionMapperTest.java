package com.flit.runtime.jaxrs;

import static org.junit.Assert.assertEquals;

import com.flit.runtime.ErrorCode;
import com.flit.runtime.FlitException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FlitExceptionMapperTest {

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private FlitExceptionMapper flitExceptionMapper = new FlitExceptionMapper();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testToResponse() {
    FlitException flit = FlitException.builder()
        .withCause(new RuntimeException("Failed"))
        .withErrorCode(ErrorCode.INTERNAL)
        .withMessage("with this message")
        .build();
    Response response = flitExceptionMapper.toResponse(flit);
    assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
    Map<String, Object> expectedResult = Map.of("msg", "with this message", "code", "internal");
    assertEquals(response.getEntity(), expectedResult);
    assertEquals(response.getMediaType(), MediaType.APPLICATION_JSON_TYPE);
  }
}
