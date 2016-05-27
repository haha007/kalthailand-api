For signing API, there must be a PKCS keystore file named "www.krungthai-axa.co.th.p12" in the same folder as this text file.
This keysotre has to be generated with openssl.

To do so, you need:
- The "full chain certificate" for krungthai-axa. It should be a txt file with extension .crt or .pem (or whatever you have)
  The content of this file should be 2 certificates. When opening with text editor, you should see 2 parts.
  Each part should begin with "-----BEGIN CERTIFICATE-----" and end with "-----END CERTIFICATE-----".
  These certificates have an expiration date and should be replaced before their expiration !
  Such file is also found in the same folder as this text file
- The private key for the cerificate file.
  Be careful, this is sensitive information, and it should never be put in github or anywhere where it might be
  retrieved by not authorized person.
  Private key is also a text file, with extension .pem and can be open with text editor.
  Unlike full chain certificate text file, there is only one part which should begin with
  "-----BEGIN RSA PRIVATE KEY-----" and end with "-----END RSA PRIVATE KEY-----"

To generate the keystore:
- put the full chain certificate file and the key file in the same folder
- rename full chain certificate file to www.krungthai-axa.co.th.ca.crt
- rename private key file to www.krungthai-axa.co.th.key.pem
- open a terminal command and go to that folder
- use command line:
    openssl pkcs12 -export -in www.krungthai-axa.co.th.ca.crt -inkey www.krungthai-axa.co.th.key.pem -out www.krungthai-axa.co.th.p12 -name krungthai-axa-cert -caname krungthai-axa-root

