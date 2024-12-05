package com.google.recaptcha.pld.pld.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.protobuf.util.JsonFormat;
import com.google.recaptchaenterprise.v1.Assessment;
import java.io.IOException;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class AssessmentJsonComponent {
  public static class Serializer extends JsonSerializer<Assessment> {
    @Override
    public void serialize(
        Assessment assessment, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
        throws IOException {
      jsonGenerator.writeString(JsonFormat.printer().print(assessment));
    }
  }

  public static class Deserializer extends JsonDeserializer<Assessment> {
    @Override
    public Assessment deserialize(
        JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
      Assessment.Builder builder = Assessment.newBuilder();
      JsonFormat.parser().merge(jsonParser.readValueAsTree().toString(), builder);
      return builder.build();
    }
  }
}
