#!/usr/bin/env bash
set -euo pipefail

HOST="localhost"
PORT="8083"
URL="http://${HOST}:${PORT}/api/chatbot"
USER_AGENT="rate-limit-test"
FORWARDED_IP="203.0.113.42"  # fixed test IP so counters are consistent
BODY='{"query": "comment proteger mes enfants contre le chantage"}'

json() { echo -n "$1"; }

phase_header() {
  echo
  echo "=================================================="
  echo "$1"
  echo "URL: $URL"
  echo "User-Agent: $USER_AGENT | X-Forwarded-For: $FORWARDED_IP"
  echo "=================================================="
}

# Phase A: Quick sanity + Resilience4j limiter probe (expect 200s currently)
phase_header "Phase A: 15 rapid requests (probe for 429 Too Many Requests)"
rc429=0
rc200=0
rcOther=0
for i in $(seq 1 16); do
  code=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$URL" \
    -H "Content-Type: application/json" \
    -H "User-Agent: $USER_AGENT" \
    -H "X-Forwarded-For: $FORWARDED_IP" \
    --data-raw "$(json "$BODY")")
  printf "Request %2d -> HTTP %s\n" "$i" "$code"
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

# Phase B: DDoS filter stress (expect 403 Forbidden after ~100 req/min)
phase_header "Phase B: 120 rapid requests to trigger DDoSProtectionFilter (expect 403 after 100)"
rc403=0
rc200b=0
rcOtherb=0
first403Index=-1
for i in $(seq 1 120); do
  code=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$URL" \
    -H "Content-Type: application/json" \
    -H "User-Agent: $USER_AGENT" \
    -H "X-Forwarded-For: $FORWARDED_IP" \
    --data-raw "$(json "$BODY")")
  printf "Request %3d -> HTTP %s\n" "$i" "$code"
  if [[ "$code" == "403" ]]; then
    ((rc403++))
    if [[ $first403Index -lt 0 ]]; then first403Index=$i; fi
  elif [[ "$code" == "200" ]]; then
    ((rc200b++))
  else
    ((rcOtherb++))
  fi
  # Very small delay keeps within ~1 minute window
  sleep 0.05
done

echo "Summary (Phase B): 200=$rc200b, 403=$rc403, other=$rcOtherb"
if [[ $first403Index -ge 0 ]]; then
  echo "First 403 observed at request #$first403Index (blacklist active)."
else
  echo "No 403 observed; DDoS filter threshold may not have been reached or timing window differed."
fi

echo
echo "Notes:"
echo "- If 429s appear in Phase A, the Resilience4j rate limiter is enforcing limits."
echo "- With current code, 429 may not appear because allowRequest() ignores the boolean from RateLimiter#acquirePermission()."
echo "- 403s in Phase B indicate the application-level DDoSProtectionFilter blacklisting after >100 requests/min from the same IP."