package com.ncj.zod.covert.handler;

import com.ncj.zod.format.ZodFormatter;
import com.ncj.zod.manager.ZodTypeHandlerManager;
import com.ncj.zod.utils.CommonUtils;
import org.openapi4j.parser.model.v3.Schema;

public class ArrayZodTypeHandler implements ZodTypeHandler {
    private final ZodTypeHandlerManager handlerManager;
    private final ZodFormatter formatter;

    public ArrayZodTypeHandler(ZodTypeHandlerManager handlerManager, ZodFormatter formatter) {
        this.handlerManager = handlerManager;
        this.formatter = formatter;
    }

    @Override
    public boolean supports(String type) {
        return "array".equals(type);
    }

    @Override
    public String handle(Schema schema, int indentLevel) {
        Schema itemSchema = schema.getItemsSchema();
        if (itemSchema == null) return "z.array(z.unknown())";

        String innerText = itemSchema.getCanonicalRef() != null
                ? CommonUtils.convertSchemaRefFormat(handlerManager.extractRefName(itemSchema.getCanonicalRef()))
                : handlerManager.convertInline(itemSchema, indentLevel + 1);

        return formatter.formatArray(innerText, indentLevel);
    }
}