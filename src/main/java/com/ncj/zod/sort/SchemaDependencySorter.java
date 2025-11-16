package com.ncj.zod.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class SchemaDependencySorter {

  /**
   * 정렬된 참조 스키마 이름 목록을 반환 (의존성에 따라 순서 보장)
   *
   * @param dependencies 스키마명 -> 참조된 스키마명 목록
   * @return 정렬된 스키마명 리스트
   */
  public static List<String> sortByDependency(Map<String, Set<String>> dependencies) {
    Map<String, Integer> inDegree = new HashMap<>();
    Map<String, Set<String>> adj = new HashMap<>();

    // 역방향으로 설정 (dep → schema)
    for (String schema : dependencies.keySet()) {
      inDegree.putIfAbsent(schema, 0); // 스키마 등록

      for (String dep : dependencies.get(schema)) {
        adj.computeIfAbsent(dep, k -> new HashSet<>()).add(schema); // dep → schema
        inDegree.put(schema, inDegree.getOrDefault(schema, 0) + 1);  // 의존받는 대상 in-degree 증가
      }
    }

    // 위상 정렬 (Kahn 알고리즘)
    Queue<String> queue = new LinkedList<>();
    for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
      if (entry.getValue() == 0) {
        queue.offer(entry.getKey());
      }
    }

    List<String> sorted = new ArrayList<>();
    while (!queue.isEmpty()) {
      String current = queue.poll();
      sorted.add(current);

      for (String neighbor : adj.getOrDefault(current, Collections.emptySet())) {
        inDegree.put(neighbor, inDegree.get(neighbor) - 1);
        if (inDegree.get(neighbor) == 0) {
          queue.offer(neighbor);
        }
      }
    }

    if (sorted.size() != inDegree.size()) {
      throw new IllegalStateException("⚠️ 스키마 의존성 순환 발생! 순서 정렬 실패");
    }

    return sorted;
  }
}
