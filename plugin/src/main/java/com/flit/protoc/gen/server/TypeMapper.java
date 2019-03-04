package com.flit.protoc.gen.server;

import com.google.protobuf.DescriptorProtos;
import com.squareup.javapoet.ClassName;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeMapper {

  // Maps the protobuf package and name to the fully qualified name of the generated Java class.
  private final Map<String, String> mapping = new HashMap<>();

  public TypeMapper() {
  }

  public TypeMapper(List<DescriptorProtos.FileDescriptorProto> files) {
    files.forEach(this::add);
  }

  public void add(DescriptorProtos.FileDescriptorProto proto) {
    proto.getMessageTypeList().forEach(m -> {
      mapping.put("." + proto.getPackage() + "." + m.getName(),
          getOuterClassOrPackageName(proto) + "." + m.getName());
    });
  }

  public ClassName get(String protobufFqcn) {
    return ClassName.bestGuess(mapping.get(protobufFqcn));
  }

  /**
   * Determine where message or service in a given proto file will be generated. Depending on the
   * java specific options in the spec, this could be either inside of an outer class, or at the top
   * level of the package.
   */
  public static String getOuterClassOrPackageName(DescriptorProtos.FileDescriptorProto proto) {
    // If no 'java_package' option is provided, the protoc compiler will default to the protobuf
    // package name.
    String packageName = proto.getOptions().hasJavaPackage() ?
        proto.getOptions().getJavaPackage() : proto.getPackage();

    // If this option is enabled protoc will generate a class for each message/service at the top
    // level of the given package space. Because message name is appended in the add method, this
    // should just return the package in that case. If there are collisions protoc should give a
    // warning/error.
    if (proto.getOptions().getJavaMultipleFiles()) {
      return packageName;
    }

    // If an outer class name is provided it should be used, otherwise we need to infer one based
    // on the same rules the protoc compiler uses.
    String outerClass = proto.getOptions().hasJavaOuterClassname() ?
        proto.getOptions().getJavaOuterClassname() : outerClassNameFromProtoName(proto);

    if (outerClass.isEmpty()) {
      throw new IllegalArgumentException("'option java_outer_classname' cannot be set to \"\".");
    }

    String fqName = String.join(".", packageName, outerClass);

    // check to see if there are any messages with this same class name as per java proto specs
    // note that we also check the services too as the protoc compiler does that as well.
    for (DescriptorProtos.DescriptorProto type : proto.getMessageTypeList()) {
      if (type.getName().equals(outerClass)) {
        return fqName + "OuterClass";
      }
    }

    for (DescriptorProtos.ServiceDescriptorProto service : proto.getServiceList()) {
      if (service.getName().equals(outerClass)) {
        return fqName + "OuterClass";
      }
    }

    return fqName;
  }

  private static String outerClassNameFromProtoName(DescriptorProtos.FileDescriptorProto proto) {
    String basename = new File(proto.getName()).getName();
    char[] classname = basename.substring(0, basename.lastIndexOf('.')).toCharArray();
    StringBuilder sb = new StringBuilder();

    char previous = '_';
    for (char c : classname) {
      if (c == '_') {
        previous = c;
        continue;
      }

      if (previous == '_') {
        sb.append(Character.toUpperCase(c));
      } else {
        sb.append(c);
      }

      previous = c;
    }

    String clazz = sb.toString();

    return clazz;
  }
}
