package com.ncj.zod.covert.handler;

public class IntegerZodTypeHandler extends NumberZodTypeHandler {
  @Override
  public boolean supports(String type) {
    return "integer".equals(type);
  }
}