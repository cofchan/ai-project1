#!/usr/bin/env bash
# Comprehensive curl-based smoke tests for the User Registration API.
# Usage: start the backend (`mvn spring-boot:run`), then run this script:
#   cd backend/api-tests && bash run_api_tests.sh
#
# Requires: curl, psql (for token retrieval from DB), jq (optional, for JSON parsing)

set -o pipefail

BASE_URL=${BASE_URL:-http://localhost:8080}
PASS=0
FAIL=0
TOTAL=0

# ──────────────────────────── helpers ────────────────────────────

check() {
  local description=$1
  local method=$2
  local url=$3
  local data=$4
  local expected=$5
  local extra_headers=$6

  TOTAL=$((TOTAL + 1))
  echo ""
  echo "─── Test $TOTAL: $description ───"
  echo "==> $method $url"

  local curl_args=(-s -w "\nHTTPSTATUS:%{http_code}" -X "$method" "$BASE_URL$url" -H "Content-Type: application/json")
  [ -n "$extra_headers" ] && curl_args+=(-H "$extra_headers")
  [ -n "$data" ] && curl_args+=(-d "$data") && echo "payload: $data"

  result=$(curl "${curl_args[@]}")
  status=$(echo "$result" | tr -d '\r' | sed -n 's/.*HTTPSTATUS://p')
  body=$(echo "$result" | sed -e 's/HTTPSTATUS:.*//g')

  echo "HTTP status: $status (expected $expected)"
  if [ "$status" == "$expected" ]; then
    echo "PASS"
    PASS=$((PASS + 1))
  else
    echo "FAIL"
    echo "response body: $body"
    FAIL=$((FAIL + 1))
  fi
  # export for callers that need the body
  LAST_BODY="$body"
  LAST_STATUS="$status"
}

# check that the response body contains a specific string (case-insensitive)
check_body_contains() {
  local description=$1
  local needle=$2
  TOTAL=$((TOTAL + 1))
  echo ""
  echo "─── Test $TOTAL: $description ───"
  if echo "$LAST_BODY" | grep -qi "$needle"; then
    echo "PASS (body contains '$needle')"
    PASS=$((PASS + 1))
  else
    echo "FAIL (body does NOT contain '$needle')"
    echo "response body: $LAST_BODY"
    FAIL=$((FAIL + 1))
  fi
}

db_query() {
  PGPASSWORD=${DB_PASSWORD:-postgres} psql -h "${DB_HOST:-localhost}" -p "${DB_PORT:-5432}" \
    -U "${DB_USER:-postgres}" -d "${DB_NAME:-registration_db}" -t -c "$1" | xargs
}

# ──────────────────────────── test data ────────────────────────────

TIMESTAMP=$(date +%s)
EMAIL="test${TIMESTAMP}@example.com"
PASSWORD="Password123!"
FULLNAME="Test User"

echo "======================================================"
echo "  User Registration API - Smoke Test Suite"
echo "======================================================"
echo "  Base URL : $BASE_URL"
echo "  Test user: $EMAIL"
echo "======================================================"


# ================================================================
#  SECTION 1: Input Validation (negative tests)
# ================================================================

echo ""
echo "======================================================="
echo " SECTION 1: Input Validation"
echo "======================================================="

# 1.1 Register — missing all fields
check "Register with empty body" \
  POST "/api/auth/register" '{}' 400

# 1.2 Register — invalid email format
check "Register with invalid email" \
  POST "/api/auth/register" \
  '{"email":"not-an-email","password":"Password123!","passwordConfirm":"Password123!","fullName":"Test"}' \
  400
check_body_contains "Error message mentions email" "email"

# 1.3 Register — weak password (no uppercase)
check "Register with weak password (no uppercase)" \
  POST "/api/auth/register" \
  '{"email":"weak@example.com","password":"password1!","passwordConfirm":"password1!","fullName":"Test"}' \
  400

# 1.4 Register — weak password (no special char)
check "Register with weak password (no special char)" \
  POST "/api/auth/register" \
  '{"email":"weak2@example.com","password":"Password1","passwordConfirm":"Password1","fullName":"Test"}' \
  400

# 1.5 Register — short password
check "Register with short password (<8 chars)" \
  POST "/api/auth/register" \
  '{"email":"short@example.com","password":"Aa1!","passwordConfirm":"Aa1!","fullName":"Test"}' \
  400

# 1.6 Register — full name too short
check "Register with 1-char full name" \
  POST "/api/auth/register" \
  '{"email":"fname@example.com","password":"Password123!","passwordConfirm":"Password123!","fullName":"X"}' \
  400

# 1.7 Login — missing password
check "Login with missing password" \
  POST "/api/auth/login" \
  '{"email":"test@example.com"}' \
  400

# 1.8 Login — invalid email format
check "Login with invalid email format" \
  POST "/api/auth/login" \
  '{"email":"not-valid","password":"Password123!"}' \
  400

# 1.9 Verify email — blank token
check "Verify email with blank token" \
  POST "/api/auth/verify-email" '{"token":""}' 400

# 1.10 Forgot password — missing email
check "Forgot password with empty body" \
  POST "/api/auth/forgot-password" '{}' 400

# 1.11 Reset password — weak new password
check "Reset password with weak new password" \
  POST "/api/auth/reset-password" \
  '{"token":"some-token","newPassword":"weak"}' \
  400

# 1.12 2FA verify — missing fields
check "2FA verify with empty body" \
  POST "/api/auth/2fa/verify" '{}' 400


# ================================================================
#  SECTION 2: Authentication Errors (negative tests)
# ================================================================

echo ""
echo "======================================================="
echo " SECTION 2: Authentication Errors"
echo "======================================================="

# 2.1 Login non-existent user
check "Login with non-existent email" \
  POST "/api/auth/login" \
  '{"email":"nobody@example.com","password":"Password123!"}' \
  401

# 2.2 Profile without token (protected endpoint)
check "GET profile without auth token" \
  GET "/api/auth/profile" '' 401

# 2.3 Profile with invalid token
check "GET profile with invalid JWT" \
  GET "/api/auth/profile" '' 401 \
  "Authorization: Bearer invalid.jwt.token"

# 2.4 Logout without token (protected endpoint)
check "POST logout without auth token" \
  POST "/api/auth/logout" '' 401

# 2.5 Verify email with invalid token
check "Verify email with bogus token" \
  POST "/api/auth/verify-email" '{"token":"bogus-invalid-token"}' 400

# 2.6 Reset password with invalid token
check "Reset password with bogus token" \
  POST "/api/auth/reset-password" \
  '{"token":"bogus-invalid-token","newPassword":"NewPass123!"}' \
  400


# ================================================================
#  SECTION 3: Happy Path — Registration & Email Verification
# ================================================================

echo ""
echo "======================================================="
echo " SECTION 3: Registration & Email Verification"
echo "======================================================="

# 3.1 Register a new user
check "Register new user" \
  POST "/api/auth/register" \
  "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"passwordConfirm\":\"$PASSWORD\",\"fullName\":\"$FULLNAME\"}" \
  201
check_body_contains "Registration response has email" "$EMAIL"

# 3.2 Duplicate registration
check "Register same email again (conflict)" \
  POST "/api/auth/register" \
  "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"passwordConfirm\":\"$PASSWORD\",\"fullName\":\"$FULLNAME\"}" \
  409

# 3.3 Login before email verification — should authenticate (2FA not yet enabled)
check "Login before email verification" \
  POST "/api/auth/login" \
  "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" \
  200

# 3.4 Login with wrong password
check "Login with wrong password" \
  POST "/api/auth/login" \
  "{\"email\":\"$EMAIL\",\"password\":\"WrongPass999!\"}" \
  401

# 3.5 Retrieve verification token from database
sleep 1
VERIF_TOKEN=$(db_query "SELECT t.token FROM email_verification_tokens t JOIN users u ON t.user_id=u.id WHERE u.email='$EMAIL' ORDER BY t.created_at DESC LIMIT 1;")
echo ""
echo "Retrieved verification token: $VERIF_TOKEN"

# 3.6 Verify email
check "Verify email with valid token" \
  POST "/api/auth/verify-email" \
  "{\"token\":\"$VERIF_TOKEN\"}" \
  200
check_body_contains "Verified user response has email" "$EMAIL"

# 3.7 Re-verify same token (should fail — token consumed)
check "Re-verify same token (should fail)" \
  POST "/api/auth/verify-email" \
  "{\"token\":\"$VERIF_TOKEN\"}" \
  400


# ================================================================
#  SECTION 4: 2FA Login Flow (email-based, auto-enabled)
# ================================================================

echo ""
echo "======================================================="
echo " SECTION 4: 2FA Login Flow"
echo "======================================================="

# After email verification, 2FA is auto-enabled. Login should return requiresTwoFA=true.
check "Login after verification (expects 2FA)" \
  POST "/api/auth/login" \
  "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" \
  200
check_body_contains "Login response indicates 2FA required" "requiresTwoFA"

# 4.1 Try 2FA verify with wrong code
check "2FA verify with wrong code" \
  POST "/api/auth/2fa/verify" \
  "{\"email\":\"$EMAIL\",\"code\":\"000000\"}" \
  401

# 4.2 Retrieve the 2FA email code from database
sleep 1
TWO_FA_CODE=$(db_query "SELECT two_fa_email_code FROM users WHERE email='$EMAIL';")
echo ""
echo "Retrieved 2FA email code: $TWO_FA_CODE"

if [ -n "$TWO_FA_CODE" ] && [ "$TWO_FA_CODE" != "" ]; then
  # 4.3 Verify with correct 2FA code
  check "2FA verify with correct code" \
    POST "/api/auth/2fa/verify" \
    "{\"email\":\"$EMAIL\",\"code\":\"$TWO_FA_CODE\"}" \
    200
  check_body_contains "2FA verify returns a JWT token" "token"

  # Save the token for authenticated requests
  JWT_TOKEN=$(echo "$LAST_BODY" | grep -o '"token":"[^"]*"' | head -1 | cut -d'"' -f4)
  echo ""
  echo "JWT token obtained: ${JWT_TOKEN:0:20}..."
else
  echo ""
  echo "WARNING: Could not retrieve 2FA code from DB - skipping 2FA verify test"
  # Fall back: no token available
  JWT_TOKEN=""
fi


# ================================================================
#  SECTION 5: Protected Endpoints (with valid JWT)
# ================================================================

echo ""
echo "======================================================="
echo " SECTION 5: Protected Endpoints"
echo "======================================================="

if [ -n "$JWT_TOKEN" ]; then
  # 5.1 GET profile
  check "GET profile with valid JWT" \
    GET "/api/auth/profile" '' 200 \
    "Authorization: Bearer $JWT_TOKEN"
  check_body_contains "Profile response has email" "$EMAIL"
  check_body_contains "Profile response has fullName" "$FULLNAME"

  # 5.2 POST logout
  check "POST logout with valid JWT" \
    POST "/api/auth/logout" '' 200 \
    "Authorization: Bearer $JWT_TOKEN"
  check_body_contains "Logout response has success message" "Logged out"
else
  echo "WARNING: No JWT token available - skipping protected endpoint tests"
fi


# ================================================================
#  SECTION 6: Password Reset Flow
# ================================================================

echo ""
echo "======================================================="
echo " SECTION 6: Password Reset Flow"
echo "======================================================="

# 6.1 Request reset
check "Forgot password for existing user" \
  POST "/api/auth/forgot-password" \
  "{\"email\":\"$EMAIL\"}" \
  200
check_body_contains "Forgot password success message" "reset"

# 6.2 Request reset for non-existent email
check "Forgot password for non-existent email" \
  POST "/api/auth/forgot-password" \
  '{"email":"nonexistent@example.com"}' \
  404

# 6.3 Retrieve reset token
sleep 1
RESET_TOKEN=$(db_query "SELECT t.token FROM password_reset_tokens t JOIN users u ON t.user_id=u.id WHERE u.email='$EMAIL' ORDER BY t.created_at DESC LIMIT 1;")
echo ""
echo "Retrieved reset token: $RESET_TOKEN"

# 6.4 Reset password with valid token
NEWPASS="NewPassword456!"
check "Reset password with valid token" \
  POST "/api/auth/reset-password" \
  "{\"token\":\"$RESET_TOKEN\",\"newPassword\":\"$NEWPASS\"}" \
  200
check_body_contains "Reset success message" "reset"

# 6.5 Reuse same reset token (should fail — token already used)
check "Reuse reset token (should fail)" \
  POST "/api/auth/reset-password" \
  "{\"token\":\"$RESET_TOKEN\",\"newPassword\":\"AnotherPass789!\"}" \
  400

# 6.6 Login with old password (should fail)
check "Login with old password after reset" \
  POST "/api/auth/login" \
  "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" \
  401

# 6.7 Login with new password
check "Login with new password after reset" \
  POST "/api/auth/login" \
  "{\"email\":\"$EMAIL\",\"password\":\"$NEWPASS\"}" \
  200


# ================================================================
#  SECTION 7: Double Reset Flow
# ================================================================

echo ""
echo "======================================================="
echo " SECTION 7: Double Reset Flow"
echo "======================================================="

# 7.1 Request another reset
check "Request second password reset" \
  POST "/api/auth/forgot-password" \
  "{\"email\":\"$EMAIL\"}" \
  200

sleep 1
RESET_TOKEN2=$(db_query "SELECT t.token FROM password_reset_tokens t JOIN users u ON t.user_id=u.id WHERE u.email='$EMAIL' AND t.used=false ORDER BY t.created_at DESC LIMIT 1;")
echo ""
echo "Retrieved second reset token: $RESET_TOKEN2"

FINALPASS="FinalPass789!"
check "Reset password with second token" \
  POST "/api/auth/reset-password" \
  "{\"token\":\"$RESET_TOKEN2\",\"newPassword\":\"$FINALPASS\"}" \
  200

# 7.2 Login with final password
check "Login with final password" \
  POST "/api/auth/login" \
  "{\"email\":\"$EMAIL\",\"password\":\"$FINALPASS\"}" \
  200


# ================================================================
#  SECTION 8: Edge Cases
# ================================================================

echo ""
echo "======================================================="
echo " SECTION 8: Edge Cases"
echo "======================================================="

# 8.1 Register with leading/trailing whitespace in email
check "Register with whitespace-padded email" \
  POST "/api/auth/register" \
  '{"email":"  spaced@example.com  ","password":"Password123!","passwordConfirm":"Password123!","fullName":"Spaced User"}' \
  201

# 8.2 Non-JSON content type
TOTAL=$((TOTAL + 1))
echo ""
echo "─── Test $TOTAL: POST register with plain text body ───"
result=$(curl -s -w "\nHTTPSTATUS:%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: text/plain" -d "not json")
status=$(echo "$result" | tr -d '\r' | sed -n 's/.*HTTPSTATUS://p')
echo "HTTP status: $status (expected 415)"
if [ "$status" == "415" ]; then
  echo "PASS"
  PASS=$((PASS + 1))
else
  echo "FAIL"
  FAIL=$((FAIL + 1))
fi

# 8.3 GET on POST-only endpoint
check "GET on POST-only register endpoint" \
  GET "/api/auth/register" '' 405

# 8.4 Token with whitespace (should be trimmed and still fail for bogus value)
check "Verify email with whitespace-padded invalid token" \
  POST "/api/auth/verify-email" \
  '{"token":"  bogus-token  "}' \
  400

# 8.5 Very long payload (overflow test)
LONG_NAME=$(python3 -c "print('A' * 300)" 2>/dev/null || printf 'A%.0s' {1..300})
check "Register with excessively long full name (>255 chars)" \
  POST "/api/auth/register" \
  "{\"email\":\"long@example.com\",\"password\":\"Password123!\",\"passwordConfirm\":\"Password123!\",\"fullName\":\"$LONG_NAME\"}" \
  400


# ════════════════════════════════════════════════════════════════
#  SUMMARY
# ════════════════════════════════════════════════════════════════

echo ""
echo "======================================================"
echo "                  TEST SUMMARY"
echo "======================================================"
printf "  Total : %d\n" $TOTAL
printf "  Passed: %d\n" $PASS
printf "  Failed: %d\n" $FAIL
echo "------------------------------------------------------"
if [ $FAIL -eq 0 ]; then
  echo "  ALL TESTS PASSED"
else
  echo "  SOME TESTS FAILED - review output above"
fi
echo "======================================================"

exit $FAIL