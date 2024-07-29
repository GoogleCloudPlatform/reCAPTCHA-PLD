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
package com.google.recaptcha.pld.pld.controllers;

import com.google.cloud.recaptcha.passwordcheck.PasswordCheckVerification;
import com.google.recaptcha.pld.pld.model.PlaintextCredentials;
import com.google.recaptcha.pld.pld.model.PldLeakedResult;
import com.google.recaptcha.pld.pld.model.PldLeakedStatus;
import com.google.recaptcha.pld.pld.services.PldService;
import com.google.recaptcha.pld.pld.services.RecaptchaContext;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PldController {

  private final PldService pldService;

  private final RecaptchaContext recaptchaContext;

  public PldController(PldService pldService, RecaptchaContext recaptchaContext) {
    this.pldService = pldService;
    this.recaptchaContext = recaptchaContext;
  }

  @PostMapping("/createAssessment")
  public CompletableFuture<PldLeakedResult> postCreateAssessment(
      @Valid @RequestBody PlaintextCredentials credentials)
      throws InterruptedException, ExecutionException {

    return pldService
        .newPasswordCheckVerification(credentials)
        .thenCompose(verification -> executePasswordLeakAssessment(verification))
        .thenApply(status -> new PldLeakedResult(status));
  }

  private CompletableFuture<PldLeakedStatus> executePasswordLeakAssessment(
      PasswordCheckVerification verification) {
    return recaptchaContext
        .createAssessmentAsync(verification)
        .thenCompose(
            assessment ->
                pldService.verifyAssessment(
                    verification, assessment.getPrivatePasswordLeakVerification()))
        .thenApply(
            pldResult ->
                pldResult.areCredentialsLeaked()
                    ? PldLeakedStatus.LEAKED
                    : PldLeakedStatus.NO_STATUS);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    return ResponseEntity.badRequest().body(errors);
  }
}
