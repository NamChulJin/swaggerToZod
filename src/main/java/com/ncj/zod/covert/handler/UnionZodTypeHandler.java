package com.ncj.zod.covert.handler;

import com.ncj.zod.format.ZodFormatter;
import com.ncj.zod.manager.ZodTypeHandlerManager;
import java.util.ArrayList;
import java.util.List;
import org.openapi4j.parser.model.v3.Schema;

public class UnionZodTypeHandler implements ZodTypeHandler {
  private final ZodTypeHandlerManager handlerManager;
  private final ZodFormatter formatter;

  public UnionZodTypeHandler(ZodTypeHandlerManager handlerManager, ZodFormatter formatter) {
    this.handlerManager = handlerManager;
    this.formatter = formatter;
  }

  @Override
  public boolean supports(String type) {
    return "union".equals(type);
  }

  @Override
  public String handle(Schema schema, int indentLevel) {
    List<Schema> schemas = schema.getAnyOfSchemas() != null
        ? schema.getAnyOfSchemas()
        : schema.getOneOfSchemas();

    if (schemas == null || schemas.isEmpty()) {
      return "z.union([])";
    }

    List<String> types = new ArrayList<>();
    for (Schema subSchema : schemas) {
      String inner = subSchema.getCanonicalRef() != null
          ? handlerManager.extractRefName(subSchema.getCanonicalRef()) + "Schema"
          : handlerManager.convertInline(subSchema, indentLevel + 1);
      types.add(inner);
    }

    return formatter.formatUnion(types, indentLevel);
  }
}