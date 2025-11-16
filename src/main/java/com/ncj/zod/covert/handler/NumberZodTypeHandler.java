package com.ncj.zod.covert.handler;

import org.openapi4j.parser.model.v3.Schema;

public class NumberZodTypeHandler implements ZodTypeHandler {
  @Override
  public boolean supports(String type) {
    return "number".equals(type);
  }

  @Override
  public String handle(Schema schema, int indentLevel) {
    StringBuilder result = new StringBuilder("z.number()");
    if (schema.getMinimum() != null) {
      result.append(".min(").append(schema.getMinimum()).append(")");
    }
    if (schema.getMaximum() != null) {
      result.append(".max(").append(schema.getMaximum()).append(")");
    }
    return "    ".repeat(indentLevel) + result.toString();
  }
}
