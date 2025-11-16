package com.ncj.zod.manager;

import com.ncj.zod.sort.SchemaDependencySorter;
import static com.ncj.zod.utils.CommonUtils.withSchemaSuffix;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class ZodSchemaCacheManager {
  private final LinkedHashMap<String, String> refNameToSchema = new LinkedHashMap<>();
  private final Map<String, String> schemaMap = new HashMap<>();
  private final Map<String, Set<String>> dependencyMap = new HashMap<>();

  public boolean isCached(String name) {
    return schemaMap.containsKey(name);
  }

  public void put(String refName, String schema) {
    if (!refNameToSchema.containsKey(refName)) {
      refNameToSchema.put(refName, schema);
      schemaMap.put(refName, schema); // 💡 export const용 이름 등록
    }
  }

  public String getSchema(String name) {
    return schemaMap.get(name);
  }

  public Set<String> getAllSchemaNames() {
    return schemaMap.keySet();
  }

  public List<String> getSortedRefNames() {
    Map<String, Set<String>> schemaDependencies = new HashMap<>();

    for (Map.Entry<String, String> entry : refNameToSchema.entrySet()) {
      String refName = entry.getKey();         // ex: Account
      String zodCode = entry.getValue();       // ex: z.object({...})

      Set<String> refs = new HashSet<>();
      for (String otherRef : refNameToSchema.keySet()) {
        if (!refName.equals(otherRef)) {
          String otherRefSchemaName = withSchemaSuffix(otherRef); // ex: JwtTokenSchema
          if (zodCode.contains(otherRefSchemaName)) {
            refs.add(otherRef);
          }
        }
      }

      schemaDependencies.put(refName, refs);
    }

    List<String> sorted = SchemaDependencySorter.sortByDependency(schemaDependencies);
    Collections.reverse(sorted);
    return sorted;
  }

  public Set<String> getDependencies(String name) {
    return dependencyMap.getOrDefault(name, new HashSet<>());
  }

  public void registerDependency(String from, String to) {
    dependencyMap.computeIfAbsent(from, k -> new HashSet<>()).add(to);
  }

  public List<String> getSortedSchemaNamesByDependencies() {
    List<String> sorted = new ArrayList<>();
    Set<String> visited = new HashSet<>();

    for (String name : schemaMap.keySet()) {
      visit(name, visited, sorted, new HashSet<>());
    }

    return sorted;
  }

  private void visit(String name, Set<String> visited, List<String> sorted, Set<String> path) {
    if (visited.contains(name)) {
      return;
    }

    if (path.contains(name)) {
      throw new IllegalStateException("Cyclic dependency detected in schema: " + name);
    }

    path.add(name);
    for (String dep : getDependencies(name)) {
      visit(dep, visited, sorted, path);
    }
    path.remove(name);

    visited.add(name);
    sorted.add(name);
  }

  /**
   * 💡 export const {name} = ... 형태로 출력
   */
  public String toSortedZodString() {
    StringBuilder sb = new StringBuilder();
    List<String> sortedNames = getSortedSchemaNamesByDependencies();
    for (String name : sortedNames) {
      String schema = schemaMap.get(name);
      sb.append("export const ").append(name).append(" = ").append(schema).append(";\n\n");
    }
    return sb.toString();
  }

  public void clear() {
    schemaMap.clear();
    dependencyMap.clear();
  }

  public Set<String> resolveDependencies(Set<String> rootRefs) {
    Set<String> visited = new HashSet<>();
    for (String ref : rootRefs) {
      resolveRecursively(ref, visited);
    }
    return visited;
  }

  private void resolveRecursively(String refName, Set<String> visited) {
    if (visited.contains(refName)) {
      return;
    }
    visited.add(refName);

    String schema = refNameToSchema.get(refName);
    if (schema == null) {
      return;
    }

    for (String otherRef : refNameToSchema.keySet()) {
      if (!refName.equals(otherRef) && schema.contains(otherRef + "Schema")) {
        resolveRecursively(otherRef, visited);
      }
    }
  }

  public List<String> getSortedRefNamesFrom(Set<String> refs) {
    Map<String, Set<String>> deps = new HashMap<>();
    for (String ref : refs) {
      String code = schemaMap.get(ref);
      Set<String> refDeps = refs.stream()
          .filter(other -> code.contains(withSchemaSuffix(other)))
          .collect(Collectors.toSet());
      deps.put(ref, refDeps);
    }
    return SchemaDependencySorter.sortByDependency(deps);
  }

}
