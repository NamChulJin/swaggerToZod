package com.ncj.zod.manager;

import com.ncj.zod.format.ZodFormatOptions;
import com.ncj.zod.format.ZodFormatter;
import com.ncj.zod.covert.handler.*;
import com.ncj.zod.sort.ZodSchemaSort;
import com.ncj.zod.utils.CommonUtils;
import lombok.Getter;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.parser.model.v3.Schema;

import java.util.*;

@Getter
public class ZodTypeHandlerManager {
    private final OpenApi3 openApi3;
    private final ZodFormatter formatter;
    private final List<ZodTypeHandler> handlers;
    private final Map<String, String> convertedCache = new LinkedHashMap<>(); // 순서 보장
    private final ZodSchemaCacheManager schemaCacheManager;
    private final Set<String> usedRefNames = new HashSet<>();
    private final Map<String, Set<String>> schemaRefDependencies = new HashMap<>();

    public ZodTypeHandlerManager(OpenApi3 openApi3, ZodSchemaCacheManager schemaCacheManager) {
        ZodFormatOptions options = ZodFormatOptions.builder()
                .compactObjectInline(true)
                .compactArrayInline(true)
                .placeOptionalOnNewLine(false)
                .useOrSyntaxInUnion(false)
                .trailingComma(true)
                .build();
        this.openApi3 = openApi3;
        this.formatter = new ZodFormatter(options);
        this.handlers = new ArrayList<>();
        this.schemaCacheManager = schemaCacheManager;
        initializeHandlers();
    }

    private void initializeHandlers() {
        handlers.add(new EnumZodTypeHandler());
        handlers.add(new StringZodTypeHandler());
        handlers.add(new BooleanZodTypeHandler());
        handlers.add(new NumberZodTypeHandler());
        handlers.add(new IntegerZodTypeHandler());
        handlers.add(new DateZodTypeHandler());

        handlers.add(new AnyOfZodTypeHandler(this));
        handlers.add(new OneOfZodTypeHandler(this));
        handlers.add(new AllOfZodTypeHandler(this, formatter));
        handlers.add(new ArrayZodTypeHandler(this, formatter));
        handlers.add(new ObjectZodTypeHandler(this, formatter));
        handlers.add(new UnionZodTypeHandler(this, formatter));

        handlers.add(new DefaultZodTypeHandler());
    }

    public void convert(Schema schema) {
        String canonicalRef = schema.getCanonicalRef();

        if (canonicalRef != null) {
            String refName = CommonUtils.lowerFirst(extractRefName(canonicalRef));
            if (!convertedCache.containsKey(refName)) {
                Schema resolved = openApi3.getComponents().getSchema(refName);
                if (resolved != null) {
                    String result = convertSchema(resolved, 0);
                    convertedCache.put(refName, result);
                    schemaCacheManager.put(refName, result);
                    usedRefNames.add(refName);

                    // 의존성 추출 및 등록
                    Set<String> innerRefs = extractReferencedRefs(resolved);
                    for (String dep : innerRefs) {
                        schemaCacheManager.registerDependency(refName, dep);
                    }
                }
            }
        }
    }

    public void convert(String name, Schema schema) {
        String refName = CommonUtils.lowerFirst(name);
        if (!convertedCache.containsKey(refName)) {
            String result = convertSchema(schema, 0);
            convertedCache.put(refName, result);
            schemaCacheManager.put(refName, result);

            // 내부 참조 추출 및 등록
            Set<String> innerRefs = extractReferencedRefs(schema);
            for (String dep : innerRefs) {
                schemaCacheManager.registerDependency(refName, dep);
            }
            schemaRefDependencies.put(refName, usedRefNames);
        }
    }

    private String convertSchema(Schema schema, int indentLevel) {
        String type = resolveType(schema);

        for (ZodTypeHandler handler : handlers) {
            if (handler.supports(type)) {
                String result = handler.handle(schema, indentLevel);
                if (Boolean.TRUE.equals(schema.getNullable())) {
                    result += ".nullable()";
                }

                if (schema.getCanonicalRef() != null) {
                    String refName = CommonUtils.lowerFirst(extractRefName(schema.getCanonicalRef()));
                    usedRefNames.add(refName); // ✅ 반드시 기록
                    schemaCacheManager.put(refName, result);
                }

                return result;
            }
        }

        return "z.unknown()";
    }

    public String convertInline(Schema schema, int indentLevel) {
        if (schema.getCanonicalRef() != null) {
            String refName = CommonUtils.lowerFirst(extractRefName(schema.getCanonicalRef()));
            usedRefNames.add(refName);  // ✅ 꼭 추가
            if (!convertedCache.containsKey(refName)) {
                Schema resolved = openApi3.getComponents().getSchema(refName);
                if (resolved != null) {
                    String result = convertSchema(resolved, indentLevel);
                    convertedCache.put(refName, result);
                    schemaCacheManager.put(refName, result);
                }
            }
            return refName + "Schema";
        }

        for (String ref : extractReferencedRefs(schema)) {
            if (!convertedCache.containsKey(ref)) {
                Schema resolved = resolveSchema(ref);
                if (resolved != null) {
                    String result = convertSchema(resolved, indentLevel);
                    convertedCache.put(ref, result);
                    schemaCacheManager.put(ref, result);
                }
            }
            usedRefNames.add(ref);
        }

        String type = resolveType(schema);

        for (ZodTypeHandler handler : handlers) {
            if (handler.supports(type)) {
                String result = handler.handle(schema, indentLevel);
                if (Boolean.TRUE.equals(schema.getNullable())) {
                    result += ".nullable()";
                }
                return result;
            }
        }

        return "z.unknown()";
    }

    private String resolveType(Schema schema) {
        if (schema.getEnums() != null && !schema.getEnums().isEmpty()) return "enum";
        if (schema.getAllOfSchemas() != null) return "allOf";
        if (schema.getAnyOfSchemas() != null) return "anyOf";
        if (schema.getOneOfSchemas() != null) return "oneOf";
        if (schema.getProperties() != null) return "object";
        if (schema.getItemsSchema() != null) return "array";
        if (schema.getType() != null) return schema.getType(); // 마지막에 검사
        return "unknown";
    }

    public String extractRefName(String canonicalRef) {
        String[] parts = canonicalRef.split("/");
        return parts[parts.length - 1];
    }

    public Schema resolveSchema(String refName) {
        if (refName == null || !openApi3.getComponents().hasSchema(refName)) {
            return null;
        }
        return openApi3.getComponents().getSchema(refName);
    }

    public String resolveSchema(Schema schema) {
        if (schema == null) return "z.unknown()";
        for (ZodTypeHandler handler : handlers) {
            if (handler.supports(schema.getType())) {
                return handler.handle(schema, 0);
            }
        }

        return "z.unknown()";
    }

    private Set<String> extractReferencedRefs(Schema schema) {
        Set<String> refs = new HashSet<>();

        if (schema.getProperties() != null) {
            for (Schema property : schema.getProperties().values()) {
                if (property.getCanonicalRef() != null) {
                    refs.add(CommonUtils.lowerFirst(extractRefName(property.getCanonicalRef())));
                }
            }
        }

        if (schema.getItemsSchema() != null && schema.getItemsSchema().getCanonicalRef() != null) {
            refs.add(CommonUtils.lowerFirst(extractRefName(schema.getItemsSchema().getCanonicalRef())));
        }

        if (schema.getAllOfSchemas() != null) {
            for (Schema sub : schema.getAllOfSchemas()) {
                if (sub.getCanonicalRef() != null) {
                    refs.add(CommonUtils.lowerFirst(extractRefName(sub.getCanonicalRef())));
                }
            }
        }

        if (schema.getOneOfSchemas() != null) {
            for (Schema sub : schema.getOneOfSchemas()) {
                if (sub.getCanonicalRef() != null) {
                    refs.add(CommonUtils.lowerFirst(extractRefName(sub.getCanonicalRef())));
                }
            }
        }

        if (schema.getAnyOfSchemas() != null) {
            for (Schema sub : schema.getAnyOfSchemas()) {
                if (sub.getCanonicalRef() != null) {
                    refs.add(CommonUtils.lowerFirst(extractRefName(sub.getCanonicalRef())));
                }
            }
        }

        return refs;
    }

    public Map<String, String> getConvertedSchemas() {
        return ZodSchemaSort.sort(convertedCache);
    }

    public String getConvertByName(String refName, Schema schema) {
        if (!convertedCache.containsKey(refName)) {
            String zod = convertSchema(schema, 0);  // indentLevel은 0
            convertedCache.put(refName, zod);
            schemaCacheManager.put(refName, zod); // 🔥 캐시에 등록
        }
        return convertedCache.get(refName);
    }
}
