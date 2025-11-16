package com.ncj.zod.covert.handler;

import com.ncj.zod.manager.ZodTypeHandlerManager;
import com.ncj.zod.utils.CommonUtils;
import java.util.List;
import org.openapi4j.parser.model.v3.Schema;

public class OneOfZodTypeHandler implements ZodTypeHandler {
  private final ZodTypeHandlerManager handlerManager;

  public OneOfZodTypeHandler(ZodTypeHandlerManager handlerManager) {
    this.handlerManager = handlerManager;
  }

  @Override
  public boolean supports(String type) {
    return "oneOf".equals(type);
  }

  @Override
  public String handle(Schema schema, int indentLevel) {
    List<Schema> oneOfSchemas = schema.getOneOfSchemas();
    if (oneOfSchemas == null || oneOfSchemas.isEmpty()) {
      return "z.unknown()";
    }

    List<String> zodSchemas = oneOfSchemas.stream()
        .map(s -> {
          if (s.getCanonicalRef() != null) {
            String refName = handlerManager.extractRefName(s.getCanonicalRef());
            handlerManager.convert(s);
            return CommonUtils.convertSchemaRefFormat(refName);
          } else {
            return handlerManager.convertInline(s, indentLevel);
          }
        })
        .toList();

    // z.A.or(B).or(C) 체인 처리
    if (zodSchemas.size() == 2) {
      return zodSchemas.get(0) + ".or(" + zodSchemas.get(1) + ")";
    } else {
      return "z.union([" + String.join(", ", zodSchemas) + "])";
    }
  }
}