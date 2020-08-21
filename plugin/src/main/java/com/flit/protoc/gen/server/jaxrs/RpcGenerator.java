package com.flit.protoc.gen.server.jaxrs;

import com.flit.protoc.gen.server.BaseGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.google.common.net.MediaType;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;

public class RpcGenerator extends BaseGenerator {

  public static final ClassName PATH = ClassName.bestGuess("javax.ws.rs.Path");
  public static final ClassName POST = ClassName.bestGuess("javax.ws.rs.POST");
  public static final ClassName PRODUCES = ClassName.bestGuess("javax.ws.rs.Produces");
  public static final ClassName CONSUMES = ClassName.bestGuess("javax.ws.rs.Consumes");
  private final String context;
  private final Builder rpcResource;

  RpcGenerator(DescriptorProtos.FileDescriptorProto proto,
      DescriptorProtos.ServiceDescriptorProto service, String context, TypeMapper mapper) {
    super(proto, service, mapper);
    this.context = getContext(context);
    this.rpcResource = TypeSpec.classBuilder(getResourceName(service))
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(
            AnnotationSpec.builder(PATH).addMember("value", "$S",
                this.context + "/" + (proto.hasPackage() ? proto.getPackage() + "." : "") + service
                    .getName()).build());
    addInstanceFields();
    addConstructor();
    service.getMethodList().forEach(this::addHandleMethod);
  }

  private void addConstructor() {
    rpcResource.addMethod(MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(getServiceInterface(), "service")
        .addStatement("this.service = service").build());
  }

  private void addHandleMethod(MethodDescriptorProto mdp) {
    ClassName inputType = mapper.get(mdp.getInputType());
    rpcResource.addMethod(MethodSpec.methodBuilder("handle" + mdp.getName())
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(POST)
        .addAnnotation(AnnotationSpec.builder(PATH)
            .addMember("value", "$S", "/" + mdp.getName())
            .build())
        .addAnnotation(AnnotationSpec.builder(PRODUCES)
            .addMember("value", "$S", MediaType.PROTOBUF.toString())
            .addMember("value", "$S", MediaType.JSON_UTF_8.toString())
            .build())
        .addAnnotation(AnnotationSpec.builder(CONSUMES)
            .addMember("value", "$S", MediaType.PROTOBUF.toString())
            .addMember("value", "$S", MediaType.JSON_UTF_8.toString())
            .build())
        .addParameter(inputType, "request")
        .addStatement("return Response.ok(service.handle$L(request)).build()", mdp.getName())
        .returns(ClassName.bestGuess("javax.ws.rs.core.Response"))
        .build());
  }

  private ClassName getResourceName(DescriptorProtos.ServiceDescriptorProto service) {
    return ClassName.get(javaPackage, "Rpc" + service.getName() + "Resource");
  }

  private void addInstanceFields() {
    rpcResource.addField(FieldSpec.builder(getServiceInterface(), "service")
        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build());
  }

  @Override
  public List<File> getFiles() {
    return Collections.singletonList(toFile(getResourceName(service), rpcResource.build()));
  }
}
