package com.ncj.zod.utils;

public class CommonUtils {

  public static String lowerFirst(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    return input.substring(0, 1).toLowerCase() + input.substring(1);
  }

  public static String upperFirst(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    return input.substring(0, 1).toUpperCase() + input.substring(1);
  }

  public static String extractRefName(String ref) {
    if (ref == null || !ref.contains("/")) {
      return ref;
    }
    return ref.substring(ref.lastIndexOf("/") + 1);
  }


  public static String withSchemaSuffix(String refName) {
    if (refName.endsWith("Schema")) {
      return refName;
    }
    return refName.replace(".", "") + "Schema";
  }

  public static String convertSchemaRefFormat(String schemaRef) {
    if (schemaRef == null || schemaRef.isEmpty()) {
      return schemaRef;
    }
    return withSchemaSuffix(lowerFirst(schemaRef));
  }
}
