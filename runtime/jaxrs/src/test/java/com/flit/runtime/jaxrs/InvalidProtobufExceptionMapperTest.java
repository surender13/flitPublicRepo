package com.flit.runtime.jaxrs;

import static org.junit.Assert.assertEquals;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InvalidProtobufExceptionMapperTest {

  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private InvalidProtobufExceptionMapper mapper = new InvalidProtobufExceptionMapper();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testToResponse() {
    InvalidProtocolBufferException exception = new InvalidProtocolBufferException(
        "something happened");
    Response response = mapper.toResponse(exception);
    assertEquals(response.getStatus(), 400);
    Map<String, Object> expectedResult = Map
        .of("msg", "something happened", "code", "invalid_argument");
    assertEquals(response.getEntity(), expectedResult);
    assertEquals(response.getMediaType(), MediaType.APPLICATION_JSON_TYPE);
  }
}
