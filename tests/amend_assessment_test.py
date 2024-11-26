import argparse
import base64
import docker
from http import HTTPStatus
import os
import requests
import sys
import time
from typing import Dict
import unittest
import urllib3
from google.cloud.recaptchaenterprise_v1 import Assessment, Event, TokenProperties

TEST_IMAGE_NAME = "test-pld-image"
TEST_CONTAINER_NAME = "test-pld"
TEST_CONTAINER_ADDRESS = "http://localhost:8080"
TEST_CONTAINER_ENDPOINT = "/amendAssessment"


env_vars = {
    "RECAPTCHA_PROJECT_ID": "",
    "RECAPTCHA_SITE_KEY": "",
    "GOOGLE_CLOUD_API_KEY": "",
}


class TestAmendAssessment(unittest.TestCase):
    @staticmethod
    def _wait_for_docker_container():
        while True:
            try:
                response = requests.get(TEST_CONTAINER_ADDRESS)
                response.raise_for_status()
            except (
                requests.exceptions.ConnectionError,
                urllib3.exceptions.NewConnectionError,
            ):
                # Container not yet ready
                time.sleep(1)
            except requests.exceptions.HTTPError as e:
                # Getting a 404 back means the container has become healthy
                assert e.response.status_code == 404
                return

    @staticmethod
    def _send_assessment(
        credentials: Dict[str, str], assessment: Assessment
    ) -> requests.Response:
        headers = {"content-type": "application/json"}
        data = {
            "credentials": credentials,
            "assessment": base64.b64encode(Assessment.serialize(assessment)).decode(
                "utf-8"
            ),
        }
        return requests.post(
            f"{TEST_CONTAINER_ADDRESS}{TEST_CONTAINER_ENDPOINT}",
            json=data,
            headers=headers,
        )

    @classmethod
    def setUpClass(cls):
        client = docker.from_env()
        try:
            # Stop any pre-existing container if it was running from a previous test
            container = client.containers.get(TEST_CONTAINER_NAME)
            container.stop()
        except docker.errors.NotFound:
            pass
        client.images.build(
            path=os.path.dirname(os.path.dirname(os.path.realpath(__file__))),
            tag=TEST_IMAGE_NAME,
            rm=True,
            pull=True,
        )
        client.containers.run(
            TEST_IMAGE_NAME,
            detach=True,
            environment=env_vars,
            name=TEST_CONTAINER_NAME,
            network_mode="host",
            remove=True,
        )
        TestAmendAssessment._wait_for_docker_container()

    def test_empty_assessment_with_leaked_creds(self):
        response = self._send_assessment(
            credentials={"username": "leakedusername", "password": "leakedpassword"},
            assessment=Assessment(
                event=Event(
                    site_key=env_vars["RECAPTCHA_SITE_KEY"],
                )
            ),
        )
        self.assertEqual(response.status_code, HTTPStatus.OK)
        response_json = response.json()
        self.assertEqual(response_json["pldLeakedStatus"], "LEAKED")
        response_assessment = Assessment.deserialize(
            base64.b64decode(response_json["assessment"])
        )
        self.assertEqual(response_assessment.token_properties.valid, False)

    def test_empty_assessment_with_unleaked_creds(self):
        response = self._send_assessment(
            credentials={
                "username": "my-test-username",
                "password": "my-test-password",
            },
            assessment=Assessment(
                event=Event(
                    site_key=env_vars["RECAPTCHA_SITE_KEY"],
                )
            ),
        )
        self.assertEqual(response.status_code, HTTPStatus.OK)
        response_json = response.json()
        self.assertEqual(response_json["pldLeakedStatus"], "NO_STATUS")
        response_assessment = Assessment.deserialize(
            base64.b64decode(response_json["assessment"])
        )
        self.assertEqual(response_assessment.token_properties.valid, False)

    def test_malformed_token_with_leaked_creds(self):
        response = self._send_assessment(
            credentials={"username": "leakedusername", "password": "leakedpassword"},
            assessment=Assessment(
                event=Event(
                    site_key=env_vars["RECAPTCHA_SITE_KEY"],
                    token="fake-token",
                )
            ),
        )
        self.assertEqual(response.status_code, HTTPStatus.OK)
        response_json = response.json()
        response_assessment = Assessment.deserialize(
            base64.b64decode(response_json["assessment"])
        )
        self.assertEqual(response_assessment.token_properties.valid, False)
        self.assertEqual(
            response_assessment.token_properties.invalid_reason,
            TokenProperties.InvalidReason.MALFORMED,
        )


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--google-cloud-api-key",
        required=True,
        help="API key with IAM permissions to call the reCAPTCHA service https://cloud.google.com/recaptcha/docs/access-control",
    )
    parser.add_argument(
        "--recaptcha-project-id",
        required=True,
        help="Project ID for your GCP project https://cloud.google.com/resource-manager/docs/creating-managing-projects",
    )
    parser.add_argument(
        "--recaptcha-site-key",
        required=True,
        help="reCAPTCHA site key https://cloud.google.com/recaptcha/docs/create-key-website",
    )
    args = parser.parse_args()
    env_vars["GOOGLE_CLOUD_API_KEY"] = args.google_cloud_api_key
    env_vars["RECAPTCHA_PROJECT_ID"] = args.recaptcha_project_id
    env_vars["RECAPTCHA_SITE_KEY"] = args.recaptcha_site_key

    unittest.main(argv=[sys.argv[0]])
