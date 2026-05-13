package org.folio.spring.messaging.model;

import static org.folio.spring.test.mock.MockMvcConstant.JSON_OBJECT;
import static org.folio.spring.test.mock.MockMvcConstant.KEY;
import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class EventTest {

  @InjectMocks
  private Event event;

  private ObjectMapper mapper;

  @BeforeEach
  void beforeEach() {
    mapper = JsonMapper.builder().build();
  }

  @Test
  void getTriggerIdWorksTest() {
    setField(event, "triggerId", VALUE);
    assertEquals(VALUE, event.getTriggerId());
  }

  @Test
  void setTriggerIdWorksTest() {
    setField(event, "triggerId", KEY);

    event.setTriggerId(VALUE);
    assertEquals(VALUE, getField(event, "triggerId"));
  }

  @Test
  void getPathPatternWorksTest() {
    setField(event, "pathPattern", VALUE);
    assertEquals(VALUE, event.getPathPattern());
  }

  @Test
  void setPathPatternWorksTest() {
    setField(event, "pathPattern", KEY);

    event.setPathPattern(VALUE);
    assertEquals(VALUE, getField(event, "pathPattern"));
  }

  @Test
  void getMethodWorksTest() {
    setField(event, "method", VALUE);
    assertEquals(VALUE, event.getMethod());
  }

  @Test
  void setMethodWorksTest() {
    setField(event, "method", KEY);

    event.setMethod(VALUE);
    assertEquals(VALUE, getField(event, "method"));
  }

  @Test
  void getTenantWorksTest() {
    setField(event, "tenant", VALUE);
    assertEquals(VALUE, event.getTenant());
  }

  @Test
  void setTenantWorksTest() {
    setField(event, "tenant", KEY);

    event.setTenant(VALUE);
    assertEquals(VALUE, getField(event, "tenant"));
  }

  @Test
  void getPathWorksTest() {
    setField(event, "path", VALUE);
    assertEquals(VALUE, event.getPath());
  }

  @Test
  void setPathWorksTest() {
    setField(event, "path", KEY);

    event.setPath(VALUE);
    assertEquals(VALUE, getField(event, "path"));
  }

  @Test
  void getHeadersWorksTest() {
    Map<String, String> headers = new HashMap<>();
    setField(event, "headers", headers);

    assertEquals(headers, event.getHeaders());
  }

  @Test
  void setHeadersWorksTest() {
    Map<String, String> headers = new HashMap<>();
    setField(event, "headers", null);

    event.setHeaders(headers);
    assertEquals(headers, getField(event, "headers"));
  }

  @Test
  void getPayloadWorksTest() throws JacksonException {
    JsonNode payload = mapper.readValue(JSON_OBJECT, JsonNode.class);
    setField(event, "payload", payload);

    assertEquals(payload, event.getPayload());
  }

  @Test
  void setPayloadWorksTest() throws JacksonException {
    JsonNode payload = mapper.readValue(JSON_OBJECT, JsonNode.class);

    setField(event, "payload", null);

    event.setPayload(payload);
    assertEquals(payload, getField(event, "payload"));
  }

  @Test
  void getProcessDefinitionIdsWorksTest() {
    List<String> processDefinitionIds = new ArrayList<>();
    setField(event, "processDefinitionIds", processDefinitionIds);

    assertEquals(processDefinitionIds, event.getProcessDefinitionIds());
  }

  @Test
  void setProcessDefinitionIdsWorksTest() {
    List<String> processDefinitionIds = new ArrayList<>();
    setField(event, "processDefinitionIds", null);

    event.setProcessDefinitionIds(processDefinitionIds);
    assertEquals(processDefinitionIds, getField(event, "processDefinitionIds"));
  }

  @Test
  void getTaskIdsWorksTest() {
    List<String> taskIds = new ArrayList<>();
    setField(event, "taskIds", taskIds);

    assertEquals(taskIds, event.getTaskIds());
  }

  @Test
  void setTaskIdsWorksTest() {
    List<String> taskIds = new ArrayList<>();
    setField(event, "taskIds", null);

    event.setTaskIds(taskIds);
    assertEquals(taskIds, getField(event, "taskIds"));
  }

}
