// Copyright 2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.recaptcha.pld.pld.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.recaptcha.pld.pld.util.AssessmentJsonComponent.Deserializer;
import com.google.recaptchaenterprise.v1.Assessment;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@Validated
public class AmendAssessmentRequest {
  @Valid private PlaintextCredentials credentials;

  @JsonDeserialize(using = Deserializer.class)
  private Assessment assessment;

  public AmendAssessmentRequest(@Valid PlaintextCredentials credentials, Assessment assessment) {
    this.credentials = credentials;
    this.assessment = assessment;
  }

  public PlaintextCredentials getCredentials() {
    return credentials;
  }

  public Assessment getAssessment() {
    return assessment;
  }
}
