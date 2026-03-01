#!/usr/bin/env bash
# Simple curl-based smoke tests for the User Registration API.
# Usage: start the backend (`mvn spring-boot:run`), then run this script:
#   cd backend/api-tests && bash run_api_tests.sh

BASE_URL=${BASE_URL:-http://localhost:8080}

# helper to perform a request and check status code
check() {
  local method=$1
  local url=$2
  local data=$3
  local expected=$4
  echo "\n==> $method $url";
  if [ -n "$data" ]; then
    echo "payload: $data";
    result=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X $method "$BASE_URL$url" -H "Content-Type: application/json" -d "$data")
  else
    result=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X $method "$BASE_URL$url")
  fi
  status=$(echo "$result" | tr -d '\r' | sed -n 's/.*HTTPSTATUS://p')
  body=$(echo "$result" | sed -e 's/HTTPSTATUS:.*//g')
  echo "HTTP status: $status (expected $expected)"
  if [ "$status" == "$expected" ]; then
    echo PASS
  else
    echo FAIL
    echo "response body: $body"
  fi
}

# Note: the following tests assume the application database is empty / reset.
# Modify the JSON bodies and tokens to match your test data.

# 1. Register a new user with a unique address each run
EMAIL="test$(date +%s)@example.com"
PASSWORD="Password123!"  # must satisfy validation rules
check POST "/api/auth/register" "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"passwordConfirm\":\"$PASSWORD\",\"fullName\":\"Test User\"}" 201

# 2. Retrieve verification token from database
sleep 1
# query the token using TCP connection settings
VERIF_TOKEN=$(PGPASSWORD=${DB_PASSWORD:-postgres} psql -h ${DB_HOST:-localhost} -p ${DB_PORT:-5432} -U ${DB_USER:-postgres} -d ${DB_NAME:-registration_db} -t -c "select t.token from email_verification_tokens t join users u on t.user_id=u.id where u.email='$EMAIL' order by t.created_at desc limit 1;" | xargs)
echo "got verification token: $VERIF_TOKEN"

# 3. Verify email
check POST "/api/auth/verify-email" "{\"token\":\"$VERIF_TOKEN\"}" 200

# 4. Login with verified user
check POST "/api/auth/login" "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" 200

# 5. Reset password via forgot-password flow
check POST "/api/auth/forgot-password" "{\"email\":\"$EMAIL\"}" 200
sleep 1
RESET1=$(PGPASSWORD=${DB_PASSWORD:-postgres} psql -h ${DB_HOST:-localhost} -p ${DB_PORT:-5432} -U ${DB_USER:-postgres} -d ${DB_NAME:-registration_db} -t -c "select t.token from password_reset_tokens t join users u on t.user_id=u.id where u.email='$EMAIL' order by t.created_at desc limit 1;" | xargs)
echo "got reset token: $RESET1"
NEWPASS="NewPass123!"
check POST "/api/auth/reset-password" "{\"token\":\"$RESET1\",\"newPassword\":\"$NEWPASS\"}" 200

# 6. Login with new password
PASSWORD="$NEWPASS"
check POST "/api/auth/login" "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" 200

# 7. Forgot password again
check POST "/api/auth/forgot-password" "{\"email\":\"$EMAIL\"}" 200
sleep 1
RESET2=$(PGPASSWORD=${DB_PASSWORD:-postgres} psql -h ${DB_HOST:-localhost} -p ${DB_PORT:-5432} -U ${DB_USER:-postgres} -d ${DB_NAME:-registration_db} -t -c "select t.token from password_reset_tokens t join users u on t.user_id=u.id where u.email='$EMAIL' order by t.created_at desc limit 1;" | xargs)
echo "got second reset token: $RESET2"
FINALPASS="FinalPass123!"
check POST "/api/auth/reset-password" "{\"token\":\"$RESET2\",\"newPassword\":\"$FINALPASS\"}" 200

# 8. Final login using final password
PASSWORD="$FINALPASS"
check POST "/api/auth/login" "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" 200


echo "\nTests completed. Review output to confirm expected behavior."
