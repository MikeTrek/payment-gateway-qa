package com.solidgate.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonMapper {

  private static final ObjectMapper INSTANCE = new ObjectMapper();

  private JsonMapper() {
  }

  public static String toJson(Object obj) {
    try {
      return INSTANCE.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("JSON serialization failed", e);
    }
  }
}
