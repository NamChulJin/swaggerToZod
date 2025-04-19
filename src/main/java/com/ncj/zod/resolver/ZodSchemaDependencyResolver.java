package com.ncj.zod.resolver;

import com.ncj.zod.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ZodSchemaDependencyResolver {
    private final Map<String, String> schemaMap; // 스키마명 -> zod 문자열
    private final Pattern refPattern = Pattern.compile("\\b(\\w+Schema)\\b");
    private final String zodFormat = "export const %s = %s\n";
    private final String zodInferFormat = "export type %s = z.infer<typeof %s>;\n";

    public ZodSchemaDependencyResolver(String fullZodText) {
        this.schemaMap = extractSchemas(fullZodText);
    }

    /**
     * 전체 문자열에서 스키마 단위로 파싱
     */
    private Map<String, String> extractSchemas(String fullZod) {
        Map<String, String> result = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("export const (\\w+Schema) = (z\\.object\\(.*?\\);)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(fullZod);

        while (matcher.find()) {
            String name = matcher.group(1); // e.g., AccountSchema
            String code = matcher.group(2); // z.object({...});
            result.put(name, code.trim());
        }
        return result;
    }

    /**
     * 참조된 모든 스키마를 탐색 (정렬 포함)
     */
    public String resolveDependencies(String rootSchemaName) {
        LinkedHashMap<String, String> ordered = new LinkedHashMap<>();
        Set<String> visited = new HashSet<>();
        dfs(rootSchemaName, visited, ordered);

        StringBuilder result = new StringBuilder();

        ordered.forEach((key, value) -> {
            result.append(String.format(zodFormat, CommonUtils.withSchemaSuffix(CommonUtils.lowerFirst(key)), value.trim()));
            result.append(String.format(zodInferFormat, CommonUtils.upperFirst(key).replace("Schema", "Type"), CommonUtils.withSchemaSuffix(CommonUtils.lowerFirst(key))));
            result.append("\n");
        });

        return result.toString().trim();
    }

    private void dfs(String current, Set<String> visited, Map<String, String> ordered) {
        if (visited.contains(current)) return;
        visited.add(current);

        String code = schemaMap.get(CommonUtils.lowerFirst(current));
        if (code == null) return;

        for (String ref : extractRefs(code)) {
            if (!ref.equals(current)) {
                dfs(ref, visited, ordered);
            }
        }

        ordered.put(current, code); // 참조 먼저 넣고 마지막에 본인 넣기
    }

    /**
     * 스키마 코드에서 참조된 다른 스키마명을 추출
     */
    private Set<String> extractRefs(String code) {
        Set<String> refs = new HashSet<>();
        Matcher matcher = refPattern.matcher(code);
        while (matcher.find()) {
            String ref = matcher.group(1);
            if (!ref.equals("z") && schemaMap.containsKey(ref)) {
                refs.add(ref);
            }
        }
        return refs;
    }

    public Set<String> getAllSchemaNames() {
        return schemaMap.keySet();
    }
}