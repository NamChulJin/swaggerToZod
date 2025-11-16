package com.ncj.zod.covert.handler;

import org.openapi4j.parser.model.v3.Schema;


public class StringZodTypeHandler implements ZodTypeHandler {
  @Override
  public boolean supports(String type) {
    return "string".equals(type);
  }

  @Override
  public String handle(Schema schema, int indentLevel) {
    StringBuilder result = new StringBuilder("z.string()");
    if (schema.getMinLength() != null) {
      result.append(".min(").append(schema.getMinLength()).append(")");
    }
    if (schema.getMaxLength() != null) {
      result.append(".max(").append(schema.getMaxLength()).append(")");
    }
    if (schema.getPattern() != null) {
      result.append(".regex(/").append(schema.getPattern()).append("/)");
    }
    return result.toString();
  }
}