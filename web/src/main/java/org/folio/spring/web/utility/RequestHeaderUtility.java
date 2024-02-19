package org.folio.spring.web.utility;

import java.util.Iterator;
import org.springframework.http.MediaType;

public class RequestHeaderUtility {

  /**
   * Determine if Accept header is missing the supported Content-Type.
   *
   * @param accept The raw HTTP Accept header value.
   * @param contentTypes The media-types, aka Content-Type that any is required to match.
   *
   * @return
   *   True if the Content-Type is not in the Accept header.
   *   False otherwise.
   */
  public static boolean unsupportedAccept(String accept, MediaType ...contentTypes) {
    Iterator<MediaType> iter = MediaType.parseMediaTypes(accept).iterator();

    while (iter.hasNext()) {
      MediaType type = iter.next();

      for (MediaType contentType : contentTypes) {
        if (contentType.isCompatibleWith(type)) {
          return false;
        }
      }
    }

    return true;
  }
}
