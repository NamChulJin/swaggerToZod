package com.ncj.zod.covert.handler;

import org.openapi4j.parser.model.v3.Schema;

public class BooleanZodTypeHandler implements ZodTypeHandler {
  @Override
  public boolean supports(String type) {
    return "boolean".equals(type);
  }

  @Override
  public String handle(Schema schema, int indentLevel) {
    String indent = "    ".repeat(indentLevel);
    return indent + "z.boolean()";
  }
}
