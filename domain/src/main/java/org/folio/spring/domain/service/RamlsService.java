package org.folio.spring.domain.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.folio.spring.domain.controller.exception.SchemaNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

@Service
public class RamlsService {

  private static final Pattern INCLUDE_MATCH_PATTERN = Pattern.compile("(?<=!include ).*");

  private static final String RAMLS_PATH = "ramls/";
  private static final String RAML_EXT = ".raml";

  private final ResourcePatternResolver resolver;

  public RamlsService(ResourcePatternResolver resolver) {
      this.resolver = resolver;
  }

  public List<String> getRamls() throws IOException {
    List<String> ramls = new ArrayList<>();
    Resource[] resources = resolver.getResources("classpath:ramls/*.raml");
    for (Resource resource : resources) {
      ramls.add(resource.getFilename());
    }
    return ramls;
  }

  public String getRamlByPath(String path, String okapiUrl) throws IOException {
    Resource resource = resolver.getResource("classpath:ramls/" + path);
    if (resource.exists()) {
      return replaceReferences(IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8.name()), okapiUrl);
    }
    throw new SchemaNotFoundException("RAML " + path + " not found");
  }

  private String replaceReferences(String raml, String okapiUrl) {
    Matcher matcher = INCLUDE_MATCH_PATTERN.matcher(raml);
    StringBuffer sb = new StringBuffer(raml.length());
    while (matcher.find()) {
      String path = matcher.group(0);
      String url = okapiUrl.replaceAll("/$", "");
      if (path.contains(RAMLS_PATH)) {
        path = path.substring(path.lastIndexOf(RAMLS_PATH) + RAMLS_PATH.length());
      }
      if (path.endsWith(RAML_EXT)) {
        matcher.appendReplacement(sb, Matcher.quoteReplacement(url + "/_/ramls?path=" + path));
      } else {
        matcher.appendReplacement(sb, Matcher.quoteReplacement(url + "/_/jsonSchemas?path=" + path));
      }
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

}
