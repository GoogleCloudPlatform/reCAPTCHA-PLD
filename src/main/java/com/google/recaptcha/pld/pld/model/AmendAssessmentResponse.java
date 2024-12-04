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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.recaptcha.pld.pld.util.AssessmentJsonComponent.Serializer;
import com.google.recaptchaenterprise.v1.Assessment;

public class AmendAssessmentResponse {
  @JsonSerialize(using = Serializer.class)
  private Assessment assessment;

  private PldLeakedStatus pldLeakedStatus;

  public AmendAssessmentResponse(Assessment assessment, PldLeakedStatus pldLeakedStatus) {
    this.assessment = assessment;
    this.pldLeakedStatus = pldLeakedStatus;
  }

  public Assessment getAssessment() {
    return assessment;
  }

  public PldLeakedStatus getPldLeakedStatus() {
    return pldLeakedStatus;
  }
}
