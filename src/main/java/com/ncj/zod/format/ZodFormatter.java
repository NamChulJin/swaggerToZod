package com.ncj.zod.format;

import java.util.List;
import java.util.Map;

public class ZodFormatter {
    private final ZodFormatOptions options;

    public ZodFormatter(ZodFormatOptions options) {
        this.options = options;
    }

    public String formatObject(Map<String, String> props, int indentLevel) {
        String indent = "    ".repeat(indentLevel);
        String childIndent = "    ".repeat(indentLevel + 1);
        StringBuilder sb = new StringBuilder(indent + "z.object({\n");

        props.forEach((key, value) -> {
            sb.append(childIndent).append(key).append(": ");
            sb.append(options.isCompactObjectInline() ? value.replaceAll("\\s+", "") : value);

            if (options.isTrailingComma() || props.size() > 1) sb.append(",");
            sb.append("\n");
        });

        sb.append(indent).append("})");
        return sb.toString();
    }

    public String formatArray(String itemType, int indentLevel) {
        return "z.array(" + (options.isCompactObjectInline() ? itemType.replaceAll("\\s+", "") : itemType) + ")";
    }

    public String formatUnion(List<String> variants, int indentLevel) {
        if (options.isUseOrSyntaxInUnion()) {
            return variants.stream().reduce((a, b) -> a + ".or(" + b + ")").orElse("z.unknown()");
        }
        return "z.union([" + String.join(", ", variants) + "])";
    }

    public String applyOptional(String expr, boolean isOptional, int indentLevel) {
        if (!isOptional) return expr;
        if (options.isPlaceOptionalOnNewLine()) {
            return expr + "\n" + "    ".repeat(indentLevel + 1) + ".optional()";
        }
        return expr + ".optional()";
    }
}