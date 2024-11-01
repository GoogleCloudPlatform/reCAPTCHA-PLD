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

public class Messages {
  public static final String MISSING_PROJECT_ID_MESSAGE =
      "Recaptcha Config Error: Project Id is required. This is supplied through the"
          + " RECAPTCHA_PROJECT_ID environment variable.";
  public static final String AUTH_REQUIRED_MESSAGE =
      "Recaptcha Config Error: No Auth was Detected. You must either supply a"
          + " GOOGLE_APPLICATION_CREDENTIALS (preffered) or GOOGLE_CLOUD_API_KEY as an"
          + " environment variable.";
  public static final String EMPTY_API_KEY_MESSAGE = "GOOGLE_CLOUD_API_KEY cannot be empty.";
  public static final String INTERNAL_CREDENTIALS_ARE_NULL_MESSAGE =
      "Internal Error -- Encrypted Credentials missing required information.";
  public static final String LOCALHOST_OR_HTTPS_ONLY_MESSAGE =
      "Forbidden Request -- HTTPS must be enabled to accept non-localhost connections.";
  public static final String INVALID_ASSESSMENT_BYTE_STREAM =
      "Invalid Argument: Provided byte stream not convertible to Assessment protobuf object.";
}
