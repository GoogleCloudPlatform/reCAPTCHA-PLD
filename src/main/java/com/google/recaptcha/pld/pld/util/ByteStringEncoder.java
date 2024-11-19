package com.google.recaptcha.pld.pld.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.recaptcha.pld.pld.model.Messages;
import com.google.recaptchaenterprise.v1.Assessment;
import java.util.Base64;

/* Convert between Assessment protobuf objects and base64 encoded byte strings.
 * https://protobuf.dev/getting-started/javatutorial/#parsing-serialization
 */
public class ByteStringEncoder {
  public static Assessment fromByteString(String bytestream) {
    try {
      return Assessment.parseFrom(Base64.getDecoder().decode(bytestream)).toBuilder().build();
    } catch (InvalidProtocolBufferException | IllegalArgumentException e) {
      throw new IllegalArgumentException(Messages.INVALID_ASSESSMENT_BYTE_STREAM);
    }
  }

  public static String toByteString(Assessment assessment) {
    return Base64.getEncoder().encodeToString(assessment.toByteArray());
  }
}
