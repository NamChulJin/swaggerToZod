package com.ncj.zod.sort;

import com.ncj.zod.utils.CommonUtils;

import java.util.*;

import static com.ncj.zod.utils.CommonUtils.withSchemaSuffix;

public class ZodSchemaSort {

    public static Map<String, String> sort(Map<String, String> convertedCache) {
        // 1. 의존성 맵 구성
        Map<String, Set<String>> dependencyMap = new HashMap<>();

        for (Map.Entry<String, String> entry : convertedCache.entrySet()) {
            String key = entry.getKey();
            String zodCode = entry.getValue();

            Set<String> dependencies = new HashSet<>();
            for (String otherKey : convertedCache.keySet()) {
                if (key.equals(otherKey)) continue;

                String refNameInCode = CommonUtils.lowerFirst(withSchemaSuffix(otherKey));

                if (zodCode.contains(refNameInCode)) {
                    dependencies.add(otherKey); // 원래 이름 기준으로 등록 (정렬 키 유지)
                }
            }

            dependencyMap.put(key, dependencies);
        }

        // 2. 위상 정렬
        List<String> sortedKeys = SchemaDependencySorter.sortByDependency(dependencyMap);

        // 3. 정렬된 순서로 LinkedHashMap 구성
        LinkedHashMap<String, String> sorted = new LinkedHashMap<>();
        for (String key : sortedKeys) {
            sorted.put(key, convertedCache.get(key));
        }

        return sorted;
    }


}
