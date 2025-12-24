openssl ecparam -genkey -name prime256v1 -noout -out private_key_ec.pem && openssl pkcs8 -topk8 -nocrypt -in private_key_ec.pem -out private_key.pem && rm private_key_ec.pem

openssl ec -in private_key.pem -pubout -outform PEM -out public_key.pem
