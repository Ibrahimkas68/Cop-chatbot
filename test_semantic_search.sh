#!/usr/bin/env bash
set -euo pipefail

HOST="localhost"
PORT="8083"
URL="http://${HOST}:${PORT}/api/chatbot"
USER_AGENT="rate-limit-test"
# Use a unique IP per run to guarantee a fresh bucket and deterministic results
LAST_OCTET=$(( (RANDOM % 200) + 10 ))
FORWARDED_IP="203.0.113.${LAST_OCTET}"
BODY='{"query": "comment proteger mes enfants contre le chantage"}'

phase_header() {
  echo
  echo "=================================================="
  echo "$1"
  echo "URL: $URL"
  echo "User-Agent: $USER_AGENT | X-Forwarded-For: $FORWARDED_IP"
  echo "=================================================="
}

# Preflight check: ensure service is reachable
if ! curl -s -o /dev/null -m 3 "$URL"; then
  echo "Service not reachable at $URL. Is the app running on port $PORT?" >&2
  exit 1
fi

# Phase A: Expect first 15 = 200, then 429 (capacity 15/minute)
phase_header "Phase A: 20 rapid requests (expect 200 for first 15, then 429)"
rc429=0
rc200=0
rcOther=0
for i in $(seq 1 20); do
  code=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$URL" \
    -H "Content-Type: application/json" \
    -H "User-Agent: $USER_AGENT" \
    -H "X-Forwarded-For: $FORWARDED_IP" \
    --data-raw "$BODY")
  printf "Request %2d -> HTTP %s\n" "$i" "$code"
  if [[ "$i" -le 15 && "$code" -ne 200 ]]; then
    echo "Error: Expected HTTP 200 for request $i, but got $code"
    exit 1
  elif [[ "$i" -gt 15 && "$code" -ne 429 ]]; then
    echo "Error: Expected HTTP 429 for request $i, but got $code"
    exit 1
  fi

  if [[ "$code" == "429" ]]; then
    ((rc429++))
  elif [[ "$code" == "200" ]]; then
    ((rc200++))
  else
    ((rcOther++))
  fi
  sleep 0.1
done

echo "Summary (Phase A): 200=$rc200, 429=$rc429, other=$rcOther"

if [[ "$rc200" -ne 15 || "$rc429" -ne 5 ]]; then
  echo "Test failed: Rate limiting is not working as expected in Phase A." >&2
  exit 1
fi

echo "Phase A passed."

# Phase B: Drive enough requests to trigger blacklist (403)
# Threshold = 100 per minute; we already sent 20, so send 100 more quickly.
phase_header "Phase B: Trigger blacklist (expect 403 once threshold exceeded)"
rc403=0
first403=-1
for i in $(seq 1 100); do
  code=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$URL" \
    -H "Content-Type: application/json" \
    -H "User-Agent: $USER_AGENT" \
    -H "X-Forwarded-For: $FORWARDED_IP" \
    --data-raw "$BODY")
  printf "[BL] Request %3d -> HTTP %s\n" "$i" "$code"
  if [[ "$code" == "403" ]]; then
    ((rc403++))
    if [[ $first403 -lt 0 ]]; then first403=$i; fi
  fi
  # very small delay to stay within the 60s window
  sleep 0.03
done

echo "Summary (Phase B): 403=$rc403"
if [[ $rc403 -lt 1 ]]; then
  echo "Test failed: Did not observe 403 blacklist responses. Check blacklist.threshold/window." >&2
  exit 1
fi

echo "Test passed: Rate limiting (429 after 15) and blacklist (403) working as expected."
