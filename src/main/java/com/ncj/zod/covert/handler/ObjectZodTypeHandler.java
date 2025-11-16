package com.ncj.zod.covert.handler;

import com.ncj.zod.format.ZodFormatter;
import com.ncj.zod.manager.ZodTypeHandlerManager;
import com.ncj.zod.utils.CommonUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openapi4j.parser.model.v3.Schema;

public class ObjectZodTypeHandler implements ZodTypeHandler {
  private final ZodTypeHandlerManager handlerManager;
  private final ZodFormatter formatter;

  public ObjectZodTypeHandler(ZodTypeHandlerManager handlerManager, ZodFormatter formatter) {
    this.handlerManager = handlerManager;
    this.formatter = formatter;
  }

  @Override
  public boolean supports(String type) {
    return "object".equals(type);
  }

  @Override
  public String handle(Schema schema, int indentLevel) {
    Map<String, String> properties = new LinkedHashMap<>();
    List<String> requiredFields = Optional.ofNullable(schema.getRequiredFields()).orElse(List.of());

    if (schema.getProperties() != null) {
      schema.getProperties().forEach((propName, propSchema) -> {
        String value;
        if (propSchema.getCanonicalRef() != null) {
          String refName = handlerManager.extractRefName(propSchema.getCanonicalRef());
          handlerManager.convert(propSchema); // ensure ref is cached
          value = CommonUtils.convertSchemaRefFormat(refName);
        } else {
          value = handlerManager.convertInline(propSchema, indentLevel + 1);
        }

        if (!requiredFields.contains(propName)) {
          value += ".optional()";
        }

        properties.put(propName, value);
      });
    }

    return formatter.formatObject(properties, indentLevel);
  }
}