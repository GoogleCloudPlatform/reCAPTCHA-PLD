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

public class CreateAmendedAssessmentResponse {

  private String assessment;

  private PldLeakedStatus pldLeakedStatus;

  public CreateAmendedAssessmentResponse(String assessment, PldLeakedStatus pldLeakedStatus) {
    this.assessment = assessment;
    this.pldLeakedStatus = pldLeakedStatus;
  }

  public String getAssessment() {
    return assessment;
  }

  public void setAssessment(String assessment) {
    this.assessment = assessment;
  }

  public PldLeakedStatus getPldLeakedResult() {
    return pldLeakedStatus;
  }

  public void setPldLeakedResult(PldLeakedStatus pldLeakedStatus) {
    this.pldLeakedStatus = pldLeakedStatus;
  }
}
