package com.ncj.zod.covert.handler;

import com.ncj.zod.format.ZodFormatter;
import com.ncj.zod.manager.ZodTypeHandlerManager;
import com.ncj.zod.utils.CommonUtils;
import java.util.ArrayList;
import java.util.List;
import org.openapi4j.parser.model.v3.Schema;

public class AllOfZodTypeHandler implements ZodTypeHandler {
  private final ZodTypeHandlerManager handlerManager;
  private final ZodFormatter formatter;

  public AllOfZodTypeHandler(ZodTypeHandlerManager handlerManager, ZodFormatter formatter) {
    this.handlerManager = handlerManager;
    this.formatter = formatter;
  }

  @Override
  public boolean supports(String type) {
    return "allOf".equals(type);
  }

  @Override
  public String handle(Schema schema, int indentLevel) {
    List<Schema> allOfSchemas = schema.getAllOfSchemas();
    if (allOfSchemas == null || allOfSchemas.isEmpty()) {
      return "z.unknown()";
    }

    List<String> parts = new ArrayList<>();
    for (Schema subSchema : allOfSchemas) {
      String part = subSchema.getCanonicalRef() != null
          ? CommonUtils.convertSchemaRefFormat(handlerManager.extractRefName(subSchema.getCanonicalRef()))
          : handlerManager.convertInline(subSchema, indentLevel + 1);
      parts.add(part);
    }

    // z.intersection(A, z.intersection(B, C)) 형태로 재귀 구성
    return formatIntersection(parts);
  }

  private String formatIntersection(List<String> parts) {
    if (parts.size() < 2) {
      return parts.get(0);
    }
    String acc = parts.get(parts.size() - 1);
    for (int i = parts.size() - 2; i >= 0; i--) {
      acc = "z.intersection(" + parts.get(i) + ", " + acc + ")";
    }
    return acc;
  }
}