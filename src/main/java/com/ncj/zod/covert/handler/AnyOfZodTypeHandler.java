package com.ncj.zod.covert.handler;

import com.ncj.zod.manager.ZodTypeHandlerManager;
import com.ncj.zod.utils.CommonUtils;
import org.openapi4j.parser.model.v3.Schema;

import java.util.List;

public class AnyOfZodTypeHandler implements ZodTypeHandler {
    private final ZodTypeHandlerManager handlerManager;

    public AnyOfZodTypeHandler(ZodTypeHandlerManager handlerManager) {
        this.handlerManager = handlerManager;
    }

    @Override
    public boolean supports(String type) {
        return "anyOf".equals(type);
    }

    @Override
    public String handle(Schema schema, int indentLevel) {
        List<Schema> anyOfSchemas = schema.getAnyOfSchemas();
        if (anyOfSchemas == null || anyOfSchemas.isEmpty()) {
            return "z.unknown()";
        }

        List<String> zodSchemas = anyOfSchemas.stream()
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

        // 2개면 or() 체인 방식
        if (zodSchemas.size() == 2) {
            return zodSchemas.get(0) + ".or(" + zodSchemas.get(1) + ")";
        }

        return "z.union([" + String.join(", ", zodSchemas) + "])";
    }
}
