# SCAFTIA

The tool built for the second assignment protects the secrecy and integrity of chat messages and files sent
using AES-CTR and HMAC-SHA256. Now we’re going to add authentication and fresh key distribution
protection to the files sent (we’ll leave chat messages alone).
The Needham-Schroeder protocol changes the structure of the tool a bit. Since Needham-Schroeder is a
centralized protocol, we will need to add a server to help with the key distribution. To minimize the change
to the communication protocol, we’ll just apply the key distribution and authentication to part of the tool’s
tasks - file sending. We certainly could expand it to work on chat messages too if we wanted.
We’ll still use HMAC-SHA256 to protect all messages (with the exception of on message below) and
files sent using an Encrypt-then-MAC scheme, regardless of whether they are sent using the shared key or
a session key. As in assignment 2, our scheme will be that all messages or files are encrypted first using
AES-CTR. Afterwards, an HMAC digest is computed over the ciphertext.
Best practices dictate that when encrypting, authenticating, and computing a MAC, separate keys should
be used (one key = one task). Therefore, we’ll add a third key to the tool (the server authentication key),
meaning that the tool’s GUI must be configurable with the following parameters:

1. A user name. A user name is a string consisting of printable UTF8 characters which will fit in a Java
string. The user name may not contain whitespace characters or the ‘@’ character.

2. The list of neighbors in the group - each neighbor has an IP address and port.

3. A (shared) text encryption password. The password is converted to an AES key by:
  (1) Convert the password to bytes using the UTF8 encoding 
  (2) Hashing the resulting bytes with SHA2-256 hash algorithm
  (3) Use the 256 output bits as the key for the AES cipher
This is the same encryption password used in the previous assignment.

4. A (shared) text MAC password. The password is converted to an HMAC secret by:
  (1) Convert the password to bytes using the UTF8 encoding
  (2) Hashing the resulting bytes with SHA2-256 hash algorithm
  (3) Use the 256 output bits as the secret for the HMAC algorithm
This is the same MAC password used in the previous assignment.

5. The IP address and port of the authentication/key distribution server.

6. A (non-shared) personal authentication password. The password is converted to an AES key by:
  (1) Convert the password to bytes using the UTF8 encoding
  (2) Hashing the resulting bytes with SHA2-256 hash algorithm
  (3) Use the 256 output bits as the key for the AES cipher
This is a new password which was not present in the previous assignments. It is known to just the
individual user and the authentication server.
