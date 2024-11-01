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

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.FixedHeaderProvider;
import com.google.cloud.recaptcha.passwordcheck.PasswordCheckVerification;
import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceClient;
import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceSettings;
import com.google.protobuf.ByteString;
import com.google.recaptcha.pld.pld.model.Messages;
import com.google.recaptcha.pld.pld.model.RecaptchaAuthMethod;
import com.google.recaptcha.pld.pld.model.RecaptchaConfig;
import com.google.recaptcha.pld.pld.model.VerificationResponse;
import com.google.recaptcha.pld.pld.util.PldEnvironment;
import com.google.recaptchaenterprise.v1.Assessment;
import com.google.recaptchaenterprise.v1.PrivatePasswordLeakVerification;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Base64;

@Service
public class RecaptchaContext {

  @Autowired private PldEnvironment env;

  @Value("Recaptcha PldClient 0.0.1")
  private String userAgent;

  private RecaptchaConfig config;
  private RecaptchaEnterpriseServiceClient recaptchaClient;

  public RecaptchaConfig getConfig() {
    return config;
  }

  public RecaptchaEnterpriseServiceClient getRecaptchaClient() {
    return recaptchaClient;
  }

  @PostConstruct
  public void initialize() throws IllegalArgumentException, IOException {
    if (env.shouldRecaptchaContextRunInit()) {
      initializeInternal();
    }
  }

  protected void initializeInternal() throws IllegalArgumentException, IOException {
    loadConfig();
    initializeClient();
  }

  @PreDestroy
  public void cleanup() {
    if (recaptchaClient != null) {
      recaptchaClient.close();
    }
  }

  private RecaptchaAuthMethod detectAuthMethod() {
    if (env.defaultCredentialsAreSet()) {
      return RecaptchaAuthMethod.DEFAULT_CREDENTIALS;
    }
    if (env.getApiKey() != null) {
      return RecaptchaAuthMethod.API_KEY;
    }
    return null;
  }

  private void loadConfig() throws IllegalArgumentException {
    RecaptchaAuthMethod authMethod = detectAuthMethod();

    RecaptchaConfig loadedConfig =
        new RecaptchaConfig.RecaptchaConfigBuilder()
            .withAuthMethod(authMethod)
            .withApiKey(env.getApiKey())
            .withProjectId(env.getProjectId())
            .build();
    this.config = loadedConfig;
  }

  private void initializeClient() throws IOException, IllegalStateException {
    if (config.getAuthMethod() == RecaptchaAuthMethod.API_KEY) {
      RecaptchaEnterpriseServiceSettings settings =
          RecaptchaEnterpriseServiceSettings.newBuilder()
              .setCredentialsProvider(NoCredentialsProvider.create())
              .setHeaderProvider(
                  FixedHeaderProvider.create(
                      "X-goog-api-key", env.getApiKey(), "User-Agent", userAgent))
              .build();
      RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create(settings);
      this.recaptchaClient = client;
      return;
    }

    if (config.getAuthMethod() == RecaptchaAuthMethod.DEFAULT_CREDENTIALS) {
      RecaptchaEnterpriseServiceSettings settings =
          RecaptchaEnterpriseServiceSettings.newBuilder()
              .setHeaderProvider(FixedHeaderProvider.create("User-Agent", userAgent))
              .build();
      RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create(settings);
      this.recaptchaClient = client;
      return;
    }

    throw new IllegalStateException(
        "Recaptcha Client was not created because no valid Auth Method was found.");
  }

  public CompletableFuture<VerificationResponse> createAssessmentAsync(
      PasswordCheckVerification clientEncryptedCredentials, String requestAssessment) {
    if (clientEncryptedCredentials.getLookupHashPrefix() == null
        || clientEncryptedCredentials.getEncryptedUserCredentialsHash() == null) {
      throw new IllegalArgumentException(Messages.INTERNAL_CREDENTIALS_ARE_NULL_MESSAGE);
    }
    return CompletableFuture.supplyAsync(
        () -> {
          PrivatePasswordLeakVerification pldVerification =
              PrivatePasswordLeakVerification.newBuilder()
                  .setLookupHashPrefix(
                      ByteString.copyFrom(clientEncryptedCredentials.getLookupHashPrefix()))
                  .setEncryptedUserCredentialsHash(
                      ByteString.copyFrom(
                          clientEncryptedCredentials.getEncryptedUserCredentialsHash()))
                  .build();

          try {
            Assessment amendedRequest =
                Assessment.parseFrom(Base64.getDecoder().decode(requestAssessment.getBytes()))
                    .toBuilder()
                    .setPrivatePasswordLeakVerification(pldVerification)
                    .build();

            Assessment responseAssessment =
                recaptchaClient.createAssessment(
                    "projects/" + config.getProjectId(), amendedRequest);

            return new VerificationResponse(responseAssessment, clientEncryptedCredentials);
          } catch (ApiException apiException) {
            throw apiException;
          } catch (InvalidProtocolBufferException InvalidProtocolBufferException) {
            throw new IllegalArgumentException(Messages.INVALID_ASSESSMENT_BYTE_STREAM);
          } catch (Exception e) {
            throw e;
          }
        });
  }

  public CompletableFuture<Assessment> createAssessmentAsync(
      PasswordCheckVerification clientEncryptedCredentials) {

    Assessment requestAssessment = Assessment.newBuilder().build();
    return this.createAssessmentAsync(
            clientEncryptedCredentials,
            Base64.getEncoder().encodeToString(requestAssessment.toByteArray()))
        .thenApply(response -> response.getAssessment());
  }
}
