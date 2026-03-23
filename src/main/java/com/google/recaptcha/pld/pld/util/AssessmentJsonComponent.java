package com.google.recaptcha.pld.pld.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.google.recaptchaenterprise.v1.Assessment;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;

@JacksonComponent
public class AssessmentJsonComponent {
  public static class Serializer extends ValueSerializer<Assessment> {
    @Override
    public void serialize(
        Assessment assessment, JsonGenerator jsonGenerator, SerializationContext serializationContext)
        throws JacksonException {
      try {
        jsonGenerator.writeString(JsonFormat.printer().print(assessment));
      } catch (InvalidProtocolBufferException e) {
        throw new RuntimeException("Failed to serialize Assessment to JSON", e);
      }
    }
  }

  public static class Deserializer extends ValueDeserializer<Assessment> {
    @Override
    public Assessment deserialize(
        JsonParser jsonParser, DeserializationContext deserializationContext)
        throws JacksonException {
      try {
        Assessment.Builder builder = Assessment.newBuilder();
        JsonFormat.parser().merge(jsonParser.readValueAsTree().toString(), builder);
        return builder.build();
      } catch (InvalidProtocolBufferException e) {
        throw new RuntimeException("Failed to deserialize Assessment from JSON", e);
      }
    }
  }
}
