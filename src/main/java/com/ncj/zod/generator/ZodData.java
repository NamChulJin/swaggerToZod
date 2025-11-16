package com.ncj.zod.generator;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZodData {
  private String schemaByFullZodString;
  private Map<String, String> refByZodMap;
}
