# reCAPTCHA Password Leak Detection | Container App Microservice (Public Preview)

The PLD Container App is a self-contained implementation of the reCAPTCHA
Password Leak Detection features built using Docker & Java Spring Boot. The app
abstracts advanced cryptography techniques to allow the detection of leaked
username / password credentials while preserving user privacy and provides a
REST-like HTTP(S) API that can be run locally using Docker.

Usernames & passwords sent to the container app are never shared with Google --
an encrypted username is sent to the service and checked against known
credential leaks, and then the re-encrypted results of those are verified by the
client.

Please note this release is currently under [Public Preview](https://cloud.google.com/products?hl=en#product-launch-stages).

## Important Security Context

Non-encrypted HTTP connections are only allowed from a localhost origin. This is
intended so that the PLD App can easily be run as a demo.

Although localhost loopback is a secure connection, it's strongly reccomended to
run the PLD App using HTTPS when accepting real end-user data.

## Before You Begin

1.  Create a project in Google Cloud Console, Enable the reCAPTCHA Enterprise
    API, Enable Billing, and either create a Google Cloud API Key to use or
    authenticate via Google Application Default Credentials.
2.  Make sure you have Docker available.

## Docker Quickstart (localhost)

To run the app in localhost only mode:

1.  Clone this repo:
     ```
      git clone https://github.com/GoogleCloudPlatform/reCAPTCHA-PLD
     ```

2.  Build the container:
     ```
      docker build . -t pld-local
     ```
    The app will spend some time testing and compiling all code. When complete
    you should have a docker image tagged `pld-local`.

3.  Run the container:
     ```
     docker run --network host \
       -e RECAPTCHA_PROJECT_ID=<project_id> \
       -e GOOGLE_CLOUD_API_KEY=<api_key> \
       pld-local
     ```

This will start the Container running on Port 8080 of the local machine.

Note: The `--network host` option is required here so that the app can correctly
exclude non-localhost traffic. Running it with an ordinary port binding
(`-p 8080`) will be blocked because the traffic coming from outside the docker
container is not seen as a local origin by the containerized app.

To test the PLD App when it's running you can use a sample cURL command:

```
curl -X POST -H "Content-Type: application/json" \
  -d '{"username":"leakedusername","password":"leakedpassword"}' \
  http://localhost:8080/createAssessment && echo ""
```

This should return the JSON Response:

```json
{"leakedStatus":"LEAKED"}
```

Alternatively there is another endpoint `/mergeAssessment` that will send a password
leak request along with an existing [CreateAssessment request](https://cloud.google.com/recaptcha/docs/reference/rest/v1/projects.assessments/create). The body of this request
will look like

```json
{
  "credentials": {
    "username": "leakedusername",
    "password": "leakedpassword"
  },
  "assessment": {
    "event": {
      "siteKey": "your-site-key",
      "token": "your-token"
    }
  }
}
```

where the value for `"assessment"` should be the json representation of an
[Assessment](https://cloud.google.com/recaptcha/docs/reference/rest/v1/projects.assessments#Assessment).

```json
{
  "pldLeakedStatus": "LEAKED",
  "assessment": {
    "tokenProperties": {
      "valid": true
    },
    "riskAnalysis": {
      "score": 0.9
    }
  }
}
```

where the value of the `"assessment"` field will again be the json representation of an
[Assessment](https://cloud.google.com/recaptcha/docs/reference/rest/v1/projects.assessments#Assessment).

## Auth | Application Default Credentials

The PLD Container App supports the use of Google's Application Default
Credentials. Those credentials need to be passed in as a
`GOOGLE_APPLICATION_CREDENTIALS` environment variable. The exact way this is
provided might vary depending on the development environment.

First, follow the public docs at
[Set up Application Default Credentials](https://cloud.google.com/docs/authentication/provide-credentials-adc).
You will need an auth strategy that leaves you with a JSON file to mount inside
the Container. For running locally, this can be created running `gcloud auth
application-default login`. In other cases, you can use Workload Identity
Federation credentials or create a Service Account Key.

*Important Note on Service Account Keys*

Service account keys are a security risk if not managed correctly. You should
[choose a more secure alternative](https://cloud.google.com/docs/authentication#auth-decision-tree)
to service account keys whenever possible. If you must authenticate with a
service account key, you are responsible for the security of the private key and
for other operations described by
[Best practices for managing service account keys](https://cloud.google.com/iam/docs/best-practices-for-managing-service-account-keys).

If your GOOGLE_APPLICATION_CREDENTIALS env var points to a file, you can mount
JSON credentials file used with the example command:

```
docker run --network host -e RECAPTCHA_PROJECT_ID=<project_id> \
  -e GOOGLE_APPLICATION_CREDENTIALS=/tmp/adc.json \
  -v $GOOGLE_APPLICATION_CREDENTIALS:/tmp/adc.json:ro pld-local
```

On Google Compute Engine VMs (such as a Cloud Shell) the
`$GOOGLE_APPLICATION_CREDENTIALS` variable is inherited from the account
runningm the VM, so might not refer to a file. In these cases, you can replace
this with an explicit path (or another Env Var) as follows:

```
docker run --network host -e RECAPTCHA_PROJECT_ID=<project_id> \
  -e GOOGLE_APPLICATION_CREDENTIALS=/tmp/adc.json \
  -v /path/to/application_default_credentials.json:/tmp/adc.json:ro pld-local
```

## HTTPS

HTTPS is required for all non-localhost traffic. The PLD App currently supports
both JKS / P12 Key Bundle or a PEM Certificate & Private Key.

IMPORTANT: In prod environments, an HTTPS Certificate Authority should be used.
The examples here for self-signed certificates are provided to show how HTTPS
behavior can be tested and verified.

Sample commands to generate a self-signed certificate are below.

To use HTTPS in the app:

1.  Mount the files into the docker image and `app/certs`. The app either looks
    for:

    a. `certs/jks.p12` with the keystore and key passwords passed in as
    environment variables.

    b. `certs/pem.crt` & `certs/pem.key`.

2.  Pass in a option to load the correct profile
    `--spring.profiles.active=https-jks` or
    `--spring.profiles.active=https-pem`.

-   JKS Certificate in `certs/jks.p12`
-   PEM Certificate in `certs/pem.crt` and private key in `certs/pem.key`.

#### JKS Key Generation

To generate a JKS / PKCS12 Key, use the the Java Keytool:

```
keytool -genkeypair -alias pld-jks -keyalg RSA -keysize 2048 -storetype pkcs12 -keystore certs/jks.p12 -validity 365
```

#### PEM Certificate & Private Key Generation

For a PEM Certificate & Key use the OpenSSL key tool following these steps:

```
# 1. Generate an SSL Key pair.
openssl genrsa -out certs/pem.key 2048
# 2. Get a signing authority for the key.
openssl req -new -key certs/pem.key -out certs/pem.csr
# 3. Sign the key to a certificate.
openssl x509 -req -days 365 -in certs/pem.csr -signkey certs/pem.key -out certs/pem.crt
```

### Sample HTTPS Commands

To run with a `jks / p12` certificate:
```
docker run -p 8443:8443 \
  -e RECAPTCHA_PROJECT_ID=<project_id> \
  -e GOOGLE_CLOUD_API_KEY=<api_key> \
  -e JKS_KEY_PASSWORD=<key_password> \
  -e JKS_KEYSTORE_PASSWORD=<keystore_password> \
  -v $(pwd)/certs:/app/certs \
  pld-local --spring.profiles.active=https-jks
```

To run with a `pem` certificate:
```
docker run -p 8443:8443 \
  -e RECAPTCHA_PROJECT_ID=<project_id> \
  -e GOOGLE_CLOUD_API_KEY=<api_key> \
  -v $(pwd)/certs:/app/certs \
  pld-local --spring.profiles.active=https-pem`
```

You may test either of these with the sample cURL command:

```
curl -X POST -H "Content-Type: application/json" \
  -d '{ "username":"leakedusername" , "password":"leakedpassword" }' \
  -k https://localhost:8443/createAssessment && echo ""
```

The `-k` option is added here as a self-signed cert, and this allows cURL to
warn and proceed. For production, use an HTTPS strategy signed by a certificate
authority instead of a self signed cert.

## Testing

To run the end-to-end tests:

> [!WARNING]
> These tests make calls to the [reCAPTCHA API](https://cloud.google.com/recaptcha/docs/apis) which
> will count towards your [billed assessments](https://cloud.google.com/security/products/recaptcha#pricing) for that GCP project.

1. [Optional] Setup a virtual environment
```
python -m venv virtualenv
source virtualenv/bin/activate
```

2. Install dependencies
```
pip install -r testing/requirements.txt
```

3. Run the unit tests
```
python tests/merge_assessment_test.py --recaptcha-project-id=your-project-id --google-cloud-api-key=your-api-key --recaptcha-site-key=your-recaptcha-site-key
```
You can run the following to see a more detailed description of the command-line arguments
```
python tests/merge_assessment_test.py --help
```

## Feedback

This repo and the container are currently under [Public Preview](https://cloud.google.com/products?hl=en#product-launch-stages).
Your feedback is important to our team & helps us improve customer experience
and value!

For issues and feature requests for the Password Leak Container, you can use our
[Github Issues](https://github.com/GoogleCloudPlatform/reCAPTCHA-PLD/issues)
page here.

To learn more about reCAPTCHA Password Leak Protection, and other ways to secure
user accounts, [Contact User Protection Cloud Sales](https://inthecloud.withgoogle.com/security-ups/contact.html).
