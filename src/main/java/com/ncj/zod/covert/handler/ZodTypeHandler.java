package com.ncj.zod.covert.handler;

import org.openapi4j.parser.model.v3.Schema;

public interface ZodTypeHandler {
    boolean supports(String type);

    String handle(Schema schema, int indentLevel);
}