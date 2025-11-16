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

  private final ZodTypeHandlerManager typeHandlerManager;
  private final ZodSchemaCacheManager schemaCacheManager;
  private final URL docUrl;
  private final OpenApi3 openApi3;

  public OpenApi3ToZodConvert(String docUrl) {
    if (docUrl == null || docUrl.isBlank()) {
      throw new IllegalArgumentException("docUrl must not be null or empty");
    }

    try {
      this.docUrl = new URL(docUrl);
      this.openApi3 = read();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid docUrl: " + docUrl, e);
    } catch (ResolutionException | ValidationException e) {
      throw new IllegalStateException("Failed to load OpenAPI document from: " + docUrl, e);
    }

    this.schemaCacheManager = new ZodSchemaCacheManager();
    this.typeHandlerManager = new ZodTypeHandlerManager(openApi3, schemaCacheManager);
  }

  private OpenApi3 read() throws ResolutionException, ValidationException {
    OpenApi3Parser openApi3Parser = new OpenApi3Parser();
    return openApi3Parser.parse(docUrl, false);
  }
}
