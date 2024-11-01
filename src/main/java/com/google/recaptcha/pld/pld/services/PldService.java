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
package com.google.recaptcha.pld.pld.services;

import com.google.cloud.recaptcha.passwordcheck.PasswordCheckResult;
import com.google.cloud.recaptcha.passwordcheck.PasswordCheckVerification;
import com.google.cloud.recaptcha.passwordcheck.PasswordCheckVerifier;
import com.google.protobuf.ByteString;
import com.google.recaptcha.pld.pld.model.CreateAmendedAssessmentResponse;
import com.google.recaptcha.pld.pld.model.PlaintextCredentials;
import com.google.recaptcha.pld.pld.model.PldLeakedStatus;
import com.google.recaptchaenterprise.v1.Assessment;
import com.google.recaptchaenterprise.v1.PrivatePasswordLeakVerification;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.Base64;

@Service
public class PldService {

  private final PasswordCheckVerifier passwordCheckVerifier;

  public PldService(PasswordCheckVerifier passwordCheckVerifier) {
    this.passwordCheckVerifier = passwordCheckVerifier;
  }

  @Async
  public CompletableFuture<PasswordCheckResult> verifyAssessment(
      PasswordCheckVerification originalVerification,
      PrivatePasswordLeakVerification assessmentData) {
    Collection<byte[]> matchPrefixes =
        assessmentData.getEncryptedLeakMatchPrefixesList().stream()
            .map(ByteString::toByteArray)
            .collect(Collectors.toList());

    byte[] reEncryptedHash = assessmentData.getReencryptedUserCredentialsHash().toByteArray();

    return passwordCheckVerifier.verify(originalVerification, reEncryptedHash, matchPrefixes);
  }

  @Async
  public CompletableFuture<CreateAmendedAssessmentResponse> populateAmendedResponse(
      PasswordCheckVerification originalVerification, Assessment assessment) {

    return this.verifyAssessment(
            originalVerification, assessment.getPrivatePasswordLeakVerification())
        .thenApply(
            pldResult ->
                new CreateAmendedAssessmentResponse(
                    Base64.getEncoder().encodeToString(assessment.toByteArray()),
                    pldResult.areCredentialsLeaked()
                        ? PldLeakedStatus.LEAKED
                        : PldLeakedStatus.NO_STATUS));
  }

  @Async
  public CompletableFuture<PasswordCheckVerification> newPasswordCheckVerification(
      PlaintextCredentials credentials) throws InterruptedException, ExecutionException {
    return passwordCheckVerifier.createVerification(
        credentials.getUsername(), credentials.getPassword());
  }
}
