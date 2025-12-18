openssl genrsa -out src/main/resources/keys/private_key.pem 2048
openssl rsa -in src/main/resources/keys/private_key.pem -pubout -out src/main/resources/keys/public_key.pem

#
# Tạo Private Key chuẩn PKCS#8
openssl genpkey -algorithm RSA -out src/main/resources/keys/private_key.pem -pkeyopt rsa_keygen_bits:2048

# Tạo Public Key từ Private Key trên
openssl rsa -in src/main/resources/keys/private_key.pem -pubout -out src/main/resources/keys/public_key.pem