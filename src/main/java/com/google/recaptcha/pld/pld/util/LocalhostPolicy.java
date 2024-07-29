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

import com.google.recaptcha.pld.pld.model.Messages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LocalhostPolicy implements HandlerInterceptor {

  @Autowired private PldEnvironment env;

  private boolean isLocalhostOrigin(HttpServletRequest request) {
    String remoteAddr = request.getRemoteAddr();
    return remoteAddr.equals("127.0.0.1")
        || remoteAddr.equals("0:0:0:0:0:0:0:1")
        || remoteAddr.equals("::1")
        || remoteAddr.equals("0000:0000:0000:0000:0000:0000:0000:0001");
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if (env.shouldAcceptRemoteConnections()) {
      return true;
    }
    if (isLocalhostOrigin(request)) {
      return true;
    }
    response.sendError(HttpServletResponse.SC_FORBIDDEN, Messages.LOCALHOST_OR_HTTPS_ONLY_MESSAGE);
    return false;
  }
}
