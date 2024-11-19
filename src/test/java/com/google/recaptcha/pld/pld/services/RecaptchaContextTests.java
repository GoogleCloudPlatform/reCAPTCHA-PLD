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
package com.google.recaptcha.pld.pld.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.cloud.recaptcha.passwordcheck.PasswordCheckVerifier;
import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceClient;
import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceSettings;
import com.google.recaptcha.pld.pld.model.Messages;
import com.google.recaptcha.pld.pld.model.RecaptchaAuthMethod;
import com.google.recaptcha.pld.pld.model.RecaptchaConfig;
import com.google.recaptcha.pld.pld.util.PldEnvironment;
import java.io.IOException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {RecaptchaContext.class, PldEnvironment.class})
@TestPropertySource(
    properties = {
      "PASSWORD_CHECK_THREAD_POOL=5",
      "RECAPTCHA_PROJECT_ID=test-proj-id",
      "GOOGLE_CLOUD_API_KEY=test-api-key",
      "pld.recaptchacontext.runinit=false",
    })
public class RecaptchaContextTests {

  @MockBean private PldEnvironment env;
  @Autowired private RecaptchaContext recaptchaContext;

  @BeforeEach
  void setupTest() {
    when(env.getProjectId()).thenReturn("test-project-id-mock");
  }

  @Test
  void shouldLoadApiCredentials() throws IllegalArgumentException, IOException {
    when(env.defaultCredentialsAreSet()).thenReturn(false);
    when(env.getApiKey()).thenReturn("test-api-key-mock");

    recaptchaContext.initializeInternal();

    RecaptchaConfig config = recaptchaContext.getConfig();
    assertEquals(RecaptchaAuthMethod.API_KEY, config.getAuthMethod());
    assertEquals("test-project-id-mock", config.getProjectId());
    assertEquals("test-api-key-mock", config.getApiKey());

    RecaptchaEnterpriseServiceClient client = recaptchaContext.getRecaptchaClient();
    RecaptchaEnterpriseServiceSettings settings = client.getSettings();
    assertEquals(
        "Recaptcha PldClient 0.0.1", settings.getHeaderProvider().getHeaders().get("User-Agent"));
    assertEquals(
        "test-api-key-mock", settings.getHeaderProvider().getHeaders().get("X-goog-api-key"));
  }

  @Test
  void shouldRejectMissingProjectId() throws IllegalArgumentException, IOException {
    when(env.getProjectId()).thenReturn(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> recaptchaContext.initializeInternal());
    assertEquals(Messages.MISSING_PROJECT_ID_MESSAGE, ex.getMessage());
  }

  @Test
  void shouldRejectMissingAuth() throws IllegalArgumentException, IOException {
    when(env.getApiKey()).thenReturn(null);
    when(env.defaultCredentialsAreSet()).thenReturn(false);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> recaptchaContext.initializeInternal());
    assertEquals(Messages.AUTH_REQUIRED_MESSAGE, ex.getMessage());
  }

  @Test
  void shouldRejectEmptyApiKey() throws IllegalArgumentException, IOException {
    when(env.getApiKey()).thenReturn("");

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> recaptchaContext.initializeInternal());
    assertEquals(Messages.EMPTY_API_KEY_MESSAGE, ex.getMessage());
  }

  @Test
  void shouldRejectInvalidAssessmentByteString() throws IllegalArgumentException, IOException {
    when(env.defaultCredentialsAreSet()).thenReturn(false);
    when(env.getApiKey()).thenReturn("test-api-key-mock");
    recaptchaContext.initializeInternal();
    PasswordCheckVerifier pldVerifier = new PasswordCheckVerifier(Executors.newFixedThreadPool(1));

    CompletionException ex =
        assertThrows(
            CompletionException.class,
            () ->
                pldVerifier
                    .createVerification("username", "fake-password")
                    .thenCompose(
                        verification ->
                            recaptchaContext.createAssessmentAsync(
                                verification, "invalid-assessment-byte-string"))
                    .join());

    assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    assertTrue(ex.getMessage().contains(Messages.INVALID_ASSESSMENT_BYTE_STREAM));
  }
}
