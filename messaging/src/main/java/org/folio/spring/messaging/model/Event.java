package org.folio.spring.messaging.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

public class Event implements Serializable {

  private static final long serialVersionUID = -4950303823935320877L;

  private String triggerId;

  private String pathPattern;

  private String method;

  private String tenant;

  private String path;

  private Map<String, String> headers;

  private JsonNode payload;

  private List<String> processDefinitionIds;

  private List<String> taskIds;

  public Event() {
    super();
  }

  // @formatter:off
  public Event(
    String triggerId,
    String pathPattern,
    String method,
    String tenant,
    String path
  ) {
  // @formatter:on
    this();
    this.triggerId = triggerId;
    this.pathPattern = pathPattern;
    this.tenant = tenant;
    this.path = path;
    this.method = method;
    this.headers = new HashMap<>();
    this.payload = JsonNodeFactory.instance.objectNode();
    this.processDefinitionIds = new ArrayList<>();
    this.taskIds = new ArrayList<>();
  }

  // @formatter:off
  public Event(
    String triggerId,
    String pathPattern,
    String method,
    String tenant,
    String path,
    Map<String, String> headers
  ) {
  // @formatter:on
    this(triggerId, pathPattern, method, tenant, path);
    this.headers = headers;
  }

  // @formatter:off
  public Event(
    String triggerId,
    String pathPattern,
    String method,
    String tenant,
    String path,
    Map<String, String> headers,
    JsonNode payload
  ) {
  // @formatter:on
    this(triggerId, pathPattern, method, tenant, path, headers);
    this.payload = payload;
  }

  public String getTriggerId() {
    return triggerId;
  }

  public void setTriggerId(String triggerId) {
    this.triggerId = triggerId;
  }

  public String getPathPattern() {
    return pathPattern;
  }

  public void setPathPattern(String pathPattern) {
    this.pathPattern = pathPattern;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getTenant() {
    return tenant;
  }

  public void setTenant(String tenant) {
    this.tenant = tenant;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public JsonNode getPayload() {
    return payload;
  }

  public void setPayload(JsonNode payload) {
    this.payload = payload;
  }

  public List<String> getProcessDefinitionIds() {
    return processDefinitionIds;
  }

  public void setProcessDefinitionIds(List<String> processDefinitionIds) {
    this.processDefinitionIds = processDefinitionIds;
  }

  public List<String> getTaskIds() {
    return taskIds;
  }

  public void setTaskIds(List<String> taskIds) {
    this.taskIds = taskIds;
  }

}
