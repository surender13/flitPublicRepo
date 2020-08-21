package com.flit.runtime.jaxrs;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
public class ProtobufMessageProvider implements MessageBodyWriter<Message>,
    MessageBodyReader<Message> {

  private static final String JSON = "json";
  private static final String PROTOBUF = "protobuf";

  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return Message.class.isAssignableFrom(type) && (
        JSON.equals(mediaType.getSubtype()) || PROTOBUF.equals(mediaType.getSubtype()));
  }

  @Override
  public long getSize(Message message, Class<?> type,
      Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    if (message == null) {
      return -1;
    }
    ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    try {
      writeTo(message, type, genericType, annotations, mediaType, null, out);
    } catch (java.io.IOException e) {
      return -1;
    }
    return out.size();
  }

  @Override
  public void writeTo(Message t, Class<?> type,
      Type genericType, Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream)
      throws IOException, WebApplicationException {
    switch (mediaType.getSubtype()) {
      case PROTOBUF:
        t.writeTo(entityStream);
        break;
      case JSON:
        entityStream
            .write(JsonFormat.printer().print(t).getBytes(StandardCharsets.UTF_8));
        break;
      default:
        throw new WebApplicationException("MediaType not supported!");
    }
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return Message.class.isAssignableFrom(type) && (
        JSON.equals(mediaType.getSubtype()) || PROTOBUF.equals(mediaType.getSubtype()));
  }

  @Override
  public Message readFrom(Class<Message> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws WebApplicationException {
    try {
      switch (mediaType.getSubtype()) {
        case PROTOBUF:
          Method m = type.getMethod("parseFrom", InputStream.class);
          return (Message) m.invoke(null, entityStream);
        case JSON:
          Message.Builder msg = (Message.Builder) type
              .getMethod("newBuilder").invoke(null);
          JsonFormat.parser()
              .merge(new InputStreamReader(entityStream), msg);
          return msg.build();
        default:
          throw new WebApplicationException("MediaType not supported!");
      }
    } catch (Exception e) {
      throw new WebApplicationException(e);
    }
  }
}
