package com.ncj.zod.covert.handler;

import org.openapi4j.parser.model.v3.Schema;

public class DefaultZodTypeHandler implements ZodTypeHandler {
  @Override
  public boolean supports(String type) {
    return true; // 항상 fallback 가능
  }

  @Override
  public String handle(Schema schema, int indentLevel) {
    return "    ".repeat(indentLevel) + "z.unknown()";
  }
}