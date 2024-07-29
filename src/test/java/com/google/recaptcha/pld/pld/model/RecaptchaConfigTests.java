// Copyright 2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.recaptcha.pld.pld.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(
    properties = {
      "PASSWORD_CHECK_THREAD_POOL=5",
      "RECAPTCHA_PROJECT_ID=test-proj-id",
      "GOOGLE_CLOUD_API_KEY=test-api-key",
      "pld.recaptchacontext.runinit=true",
    })
public class RecaptchaConfigTests {

  private static String testProjId = "test-project-id";
  private static String testApiKey = "test-api-key";

  @Test
  void shouldPassValidApiKeyConfig() {
    RecaptchaConfig config =
        new RecaptchaConfig.RecaptchaConfigBuilder()
            .withProjectId(testProjId)
            .withAuthMethod(RecaptchaAuthMethod.API_KEY)
            .withApiKey(testApiKey)
            .build();

    assertEquals(config.getProjectId(), testProjId);
    assertEquals(config.getAuthMethod(), RecaptchaAuthMethod.API_KEY);
    assertEquals(config.getApiKey(), testApiKey);
  }

  @Test
  void shouldPassValidDefaultConfig() {
    RecaptchaConfig config =
        new RecaptchaConfig.RecaptchaConfigBuilder()
            .withProjectId(testProjId)
            .withAuthMethod(RecaptchaAuthMethod.DEFAULT_CREDENTIALS)
            .build();

    assertEquals(config.getProjectId(), testProjId);
    assertEquals(config.getAuthMethod(), RecaptchaAuthMethod.DEFAULT_CREDENTIALS);
  }

  @Test
  void shouldRejectMissingProjId() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new RecaptchaConfig.RecaptchaConfigBuilder()
                    .withAuthMethod(RecaptchaAuthMethod.DEFAULT_CREDENTIALS)
                    .build());

    assertEquals(Messages.MISSING_PROJECT_ID_MESSAGE, ex.getMessage());
  }

  @Test
  void shouldRejectMissingAuth() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> new RecaptchaConfig.RecaptchaConfigBuilder().withProjectId(testProjId).build());

    assertEquals(Messages.AUTH_REQUIRED_MESSAGE, ex.getMessage());
  }

  @Test
  void shouldRejectEmptyApiKey() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new RecaptchaConfig.RecaptchaConfigBuilder()
                    .withProjectId(testProjId)
                    .withAuthMethod(RecaptchaAuthMethod.API_KEY)
                    .withApiKey("")
                    .build());

    assertEquals(Messages.EMPTY_API_KEY_MESSAGE, ex.getMessage());
  }
}
