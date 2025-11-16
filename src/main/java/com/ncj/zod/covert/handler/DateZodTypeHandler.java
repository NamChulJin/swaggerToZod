package com.ncj.zod.covert.handler;

import org.openapi4j.parser.model.v3.Schema;

public class DateZodTypeHandler implements ZodTypeHandler {
  @Override
  public boolean supports(String type) {
    return "date".equals(type);
  }

  @Override
  public String handle(Schema schema, int indentLevel) {
    return "    ".repeat(indentLevel) + "z.date()";
  }
}