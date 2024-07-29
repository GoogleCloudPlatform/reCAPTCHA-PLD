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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PldEnvironment {

  @Value("${RECAPTCHA_PROJECT_ID:#{null}}")
  private String projectId;

  @Value("${GOOGLE_CLOUD_API_KEY:#{null}}")
  private String apiKey;

  @Value("${PASSWORD_CHECK_THREAD_POOL:10}")
  private int passwordCheckThreadPoolSize;

  @Value("${pld.recaptchacontext.runinit:true}")
  private boolean recaptchaContextRunInit;

  @Value("${pld.useHttps:false}")
  private boolean useHttps;

  public boolean shouldAcceptRemoteConnections() {
    return useHttps;
  }

  public boolean shouldRecaptchaContextRunInit() {
    return recaptchaContextRunInit;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getApiKey() {
    return apiKey;
  }

  public int getPasswordCheckThreadPoolSize() {
    return passwordCheckThreadPoolSize;
  }

  public Boolean defaultCredentialsAreSet() {
    return System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null;
  }
}
