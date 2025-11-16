package com.ncj.zod.format;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ZodFormatOptions {
  private boolean compactObjectInline;       // z.object({ a: z.string(), b: z.number() }) 한 줄
  private boolean compactArrayInline;        // z.array(z.string()) 한 줄
  private boolean placeOptionalOnNewLine;    // z.string().optional() 줄 개행 여부
  private boolean useOrSyntaxInUnion;        // z.string().or(z.number()) vs z.union([...])
  private boolean trailingComma;              // 마지막 항목에도 , 여부
}