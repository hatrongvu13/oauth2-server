openssl genrsa -out src/main/resources/keys/private_key.pem 2048

# Convert sang PKCS#8 format (QUAN TRỌNG!)
openssl pkcs8 -topk8 -inform PEM -outform PEM \
  -in src/main/resources/keys/private_key.pem \
  -out src/main/resources/keys/private_key_pkcs8.pem \
  -nocrypt

# Replace file cũ
mv src/main/resources/keys/private_key_pkcs8.pem src/main/resources/keys/private_key.pem

# Generate public key
openssl rsa -in src/main/resources/keys/private_key.pem \
  -pubout -out src/main/resources/keys/public_key.pem

# Java generater
mkdir -p src/main/resources/keys
javac GenerateKeys.java
java GenerateKeys
rm GenerateKeys.class GenerateKeys.java