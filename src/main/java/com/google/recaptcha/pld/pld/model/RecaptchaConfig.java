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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public class RecaptchaConfig {

  @NotBlank(message = "Project Id is required.")
  private String projectId;

  @NotNull(message = "An Auth method is required")
  private RecaptchaAuthMethod authMethod;

  private String apiKey;

  private RecaptchaConfig(RecaptchaConfigBuilder builder) {
    this.projectId = builder.projectId;
    this.authMethod = builder.authMethod;
    this.apiKey = builder.apiKey;
  }

  public static class RecaptchaConfigBuilder {
    private String projectId;
    private RecaptchaAuthMethod authMethod;
    private String apiKey;

    public RecaptchaConfigBuilder withProjectId(String projectId) {
      this.projectId = projectId;
      return this;
    }

    public RecaptchaConfigBuilder withAuthMethod(RecaptchaAuthMethod authMethod) {
      this.authMethod = authMethod;
      return this;
    }

    public RecaptchaConfigBuilder withApiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    private Boolean nullOrEmpty(String s) {
      return (s == null || s.isEmpty());
    }

    public RecaptchaConfig build() throws IllegalArgumentException {
      if (nullOrEmpty(projectId)) {
        throw new IllegalArgumentException(Messages.MISSING_PROJECT_ID_MESSAGE);
      }
      if (authMethod == null) {
        throw new IllegalArgumentException(Messages.AUTH_REQUIRED_MESSAGE);
      }
      if (authMethod == RecaptchaAuthMethod.API_KEY && nullOrEmpty(apiKey)) {
        throw new IllegalArgumentException(Messages.EMPTY_API_KEY_MESSAGE);
      }

      return new RecaptchaConfig(this);
    }
  }

  public String getProjectId() {
    return projectId;
  }

  public RecaptchaAuthMethod getAuthMethod() {
    return authMethod;
  }

  public String getApiKey() {
    return apiKey;
  }
}
