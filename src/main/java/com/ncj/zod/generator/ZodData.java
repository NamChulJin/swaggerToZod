package com.ncj.zod.generator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZodData {
    private String schemaByFullZodString;
    private Map<String, String> refByZodMap;
}
