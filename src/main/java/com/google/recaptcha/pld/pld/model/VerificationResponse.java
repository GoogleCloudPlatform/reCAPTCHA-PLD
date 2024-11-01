package com.google.recaptcha.pld.pld.model;

import com.google.recaptchaenterprise.v1.Assessment;
import com.google.cloud.recaptcha.passwordcheck.PasswordCheckVerification;

public class VerificationResponse {

  private Assessment assessment;

  private PasswordCheckVerification passwordCheckVerification;

  public VerificationResponse(
      Assessment assessment, PasswordCheckVerification passwordCheckVerification) {
    this.assessment = assessment;
    this.passwordCheckVerification = passwordCheckVerification;
  }

  public Assessment getAssessment() {
    return assessment;
  }

  public void setAssessment(Assessment assessment) {
    this.assessment = assessment;
  }

  public PasswordCheckVerification getPasswordCheckVerification() {
    return passwordCheckVerification;
  }

  public void setPasswordCheckVerification(PasswordCheckVerification passwordCheckVerification) {
    this.passwordCheckVerification = passwordCheckVerification;
  }
}
