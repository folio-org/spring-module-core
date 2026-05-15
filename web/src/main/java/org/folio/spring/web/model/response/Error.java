package org.folio.spring.web.model.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "message", "type", "code", "parameters" })
public class Error {

  @JsonProperty("message")
  @NotNull
  private String message;

  @JsonProperty("type")
  private String type;

  @JsonProperty("code")
  private String code;

  @JsonProperty("parameters")
  @Valid
  private List<Parameter> parameters = new ArrayList<>();

  @JsonIgnore
  @Valid
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  @JsonProperty("message")
  public void setMessage(String message) {
    this.message = message;
  }

  public Error withMessage(String message) {
    this.message = message;
    return this;
  }

  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  public Error withType(String type) {
    this.type = type;
    return this;
  }

  @JsonProperty("code")
  public String getCode() {
    return code;
  }

  @JsonProperty("code")
  public void setCode(String code) {
    this.code = code;
  }

  public Error withCode(String code) {
    this.code = code;
    return this;
  }

  @JsonProperty("parameters")
  public List<Parameter> getParameters() {
    return parameters;
  }

  @JsonProperty("parameters")
  public void setParameters(List<Parameter> parameters) {
    this.parameters = parameters;
  }

  public Error withParameters(List<Parameter> parameters) {
    this.parameters = parameters;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public Error withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

}
