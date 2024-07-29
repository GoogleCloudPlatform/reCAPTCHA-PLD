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
package com.google.recaptcha.pld.pld;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
    properties = {
      "PASSWORD_CHECK_THREAD_POOL=5",
      "RECAPTCHA_PROJECT_ID=test-proj-id",
      "GOOGLE_CLOUD_API_KEY=test-api-key",
      "pld.recaptchacontext.runinit=true",
    })
class PldApplicationTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private ExecutorService passwordCheckExecutorService;

  @Test
  void contextLoads() {}

  @Test
  void validCreateAssessmentSucceeds() throws Exception {

    // Only lookupHashPrefix is determinative, the other values will change on each invocation.
    mockMvc
        .perform(
            post("/createAssessment")
                .contentType("application/json")
                .content(" { \"username\":\"usernameABC\", \"password\":\"password123\" } "))
        .andExpect(status().isOk());
  }

  @Test
  void shouldRejectInvalidPlaintextCredentials() throws Exception {
    mockMvc
        .perform(post("/createAssessment").contentType("application/json").content("{}"))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(
            post("/createAssessment")
                .contentType("application/json")
                .content(" { \"username\":\"usernameABC\"} "))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(
            post("/createAssessment")
                .contentType("application/json")
                .content("{ \"password\":\"password123\" }"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testThreadPoolCount() {
    final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) passwordCheckExecutorService;
    assertEquals(5, threadPool.getCorePoolSize());
  }
}
