package com.ncj.zod.covert.handler;

import org.openapi4j.parser.model.v3.Schema;

public class EnumZodTypeHandler implements ZodTypeHandler {
  @Override
  public boolean supports(String type) {
    return "enum".equals(type); // 우리가 enum으로 판별한 타입만 받음
  }

  @Override
  public String handle(Schema schema, int indentLevel) {
    String indent = "    ".repeat(indentLevel);
    if (schema.getEnums() == null || schema.getEnums().isEmpty()) {
      return indent + "z.string()"; // fallback
    }

    StringBuilder sb = new StringBuilder(indent + "z.enum([");
    for (int i = 0; i < schema.getEnums().size(); i++) {
      Object val = schema.getEnums().get(i);
      sb.append("\"").append(val.toString()).append("\"");
      if (i < schema.getEnums().size() - 1) {
        sb.append(", ");
      }
    }
    sb.append("])");

    return sb.toString();
  }
}
