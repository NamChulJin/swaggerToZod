package com.ncj.zod.covert;

import com.ncj.zod.manager.ZodSchemaCacheManager;
import com.ncj.zod.manager.ZodTypeHandlerManager;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.Getter;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.OpenApi3;

@Getter
public class OpenApi3ToZodConvert {
  private final String docUrl;
  private final ZodTypeHandlerManager typeHandlerManager;
  private final ZodSchemaCacheManager schemaCacheManager;
  private OpenApi3 openApi3;

  public OpenApi3ToZodConvert(String docUrl) throws ResolutionException, MalformedURLException, ValidationException {
    this.docUrl = docUrl;
    this.openApi3 = read();
    this.schemaCacheManager = new ZodSchemaCacheManager();
    this.typeHandlerManager = new ZodTypeHandlerManager(openApi3, schemaCacheManager); // 캐시 넘김
  }


  private OpenApi3 read() throws MalformedURLException, ResolutionException, ValidationException {
    OpenApi3Parser openApi3Parser = new OpenApi3Parser();
    return openApi3Parser.parse(new URL(docUrl), false);
  }
}
