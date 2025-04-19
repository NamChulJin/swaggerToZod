package com.ncj.zod.generator;

import com.ncj.zod.manager.ZodTypeHandlerManager;
import com.ncj.zod.resolver.ZodSchemaDependencyResolver;
import com.ncj.zod.utils.CommonUtils;
import lombok.Getter;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.parser.model.v3.Schema;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class ZodGenerator {
    private final OpenApi3 openApi3;
    private final ZodTypeHandlerManager handlerManager;
    private final ZodSchemaDependencyResolver resolver;

    public ZodGenerator(OpenApi3 openApi3, ZodTypeHandlerManager handlerManager) {
        this.openApi3 = openApi3;
        this.handlerManager = handlerManager;
        this.resolver = new ZodSchemaDependencyResolver(generateZodSchemas());
    }

    public Map<String, String> getConvertedSchemas() {
        return handlerManager.getConvertedSchemas();
    }

    public ZodData generateZodData(){
        return ZodData.builder()
                .schemaByFullZodString(generateZodSchemas())
                .refByZodMap(generateZodSchemaMapByRefNames())
                .build();
    }

    public String generateZodSchemas() {
        Map<String, Schema> schemas = openApi3.getComponents().getSchemas();

        // 먼저 모든 스키마를 캐시에 넣어둔다
        schemas.forEach(handlerManager::convert);

        StringBuilder sb = new StringBuilder("import { z } from 'zod';\n\n");

        handlerManager.getConvertedSchemas().forEach((name, zodSchema) -> {
            String cleanName = name.replace(".", "");
            sb.append("export const ")
                    .append(CommonUtils.lowerFirst(cleanName))
                    .append("Schema = ")
                    .append(zodSchema.trim())
                    .append(";\n\n");


            sb.append("export type ")
                    .append(CommonUtils.upperFirst(cleanName))
                    .append("Type")
                    .append(" = z.infer<typeof ")
                    .append(CommonUtils.lowerFirst(cleanName))
                    .append("Schema>;\n\n");

        });

        return sb.toString().trim(); // 마지막 공백 제거
    }

    public List<String> getRefNames() {
        if (openApi3 == null || openApi3.getComponents() == null || openApi3.getComponents().getSchemas() == null) {
            return List.of();
        }

        Map<String, Schema> schemas = openApi3.getComponents().getSchemas();

        return schemas.keySet()
                .stream()
                .filter(name -> name != null && !name.isBlank())
                .map(name -> name.replace(".", ""))
                .sorted()
                .collect(Collectors.toList());
    }


    public Map<String, String> generateZodSchemaMapByRefNames() {
        Map<String, String> result = new LinkedHashMap<>();
        getRefNames().forEach(name -> {
            String refName = name.replace(".", "");
            String code = resolver.resolveDependencies(CommonUtils.withSchemaSuffix(name));
            result.put(refName, code);
        });
        return result;
    }





}
