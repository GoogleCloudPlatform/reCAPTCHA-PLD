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
package com.google.recaptcha.pld.pld.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.recaptcha.pld.pld.model.Messages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {PldEnvironment.class, LocalhostPolicy.class})
@TestPropertySource(
    properties = {
      "PASSWORD_CHECK_THREAD_POOL=5",
      "RECAPTCHA_PROJECT_ID=test-proj-id",
      "GOOGLE_CLOUD_API_KEY=test-api-key",
      "pld.recaptchacontext.runinit=false",
    })
public class LocalhostPolicyTests {
  @MockBean private PldEnvironment env;

  @Autowired private LocalhostPolicy policy;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Test
  void shouldAllowHttps() throws Exception {
    when(env.shouldAcceptRemoteConnections()).thenReturn(true);

    Boolean result = policy.preHandle(null, response, null);
    assertTrue(result);
  }

  @Test
  void shouldAllowLocalhostIPv4() throws Exception {
    when(env.shouldAcceptRemoteConnections()).thenReturn(false);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");

    Boolean result = policy.preHandle(request, response, null);
    assertTrue(result);
  }

  @Test
  void shouldAllowLocalhostIPv6() throws Exception {
    when(env.shouldAcceptRemoteConnections()).thenReturn(false);
    when(request.getRemoteAddr()).thenReturn("::1");

    Boolean result = policy.preHandle(request, response, null);
    assertTrue(result);
  }

  @Test
  void shouldDenyRemoteWithoutHttps() throws Exception {
    when(env.shouldAcceptRemoteConnections()).thenReturn(false);
    when(request.getRemoteAddr()).thenReturn("8.8.8.8");

    Boolean result = policy.preHandle(request, response, null);
    assertFalse(result);
    verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, Messages.LOCALHOST_OR_HTTPS_ONLY_MESSAGE);
  }
}
