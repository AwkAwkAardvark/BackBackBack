#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <run_id>"
  exit 1
fi

RUN_ID="$1"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BASE_DIR="${ROOT_DIR}/perf/results/${RUN_ID}"
BEFORE_JTL="${BASE_DIR}/before/results.jtl"
AFTER_JTL="${BASE_DIR}/after/results.jtl"
SUMMARY_MD="${BASE_DIR}/comparison-summary.md"
API_DELTA_CSV="${BASE_DIR}/comparison-api-delta.csv"
RUNTIME_DELTA_CSV="${BASE_DIR}/comparison-runtime-delta.csv"
BEFORE_METRICS_TSV="${BASE_DIR}/before/runtime-metrics-summary.tsv"
AFTER_METRICS_TSV="${BASE_DIR}/after/runtime-metrics-summary.tsv"

if [[ ! -f "${BEFORE_JTL}" || ! -f "${AFTER_JTL}" ]]; then
  echo "before/after JTL not found under ${BASE_DIR}"
  exit 1
fi

summarize_jtl() {
  local jtl_file="$1"
  awk -F',' '
    $1 == "timeStamp" { next }
    {
      label = $3
      elapsed = $2 + 0
      success = ($8 == "true")

      count[label]++
      total[label] += elapsed
      if (!success) {
        error[label]++
      }

      values[label] = values[label] " " elapsed

      if (!(label in minTs) || $1 < minTs[label]) {
        minTs[label] = $1
      }
      if (!(label in maxTs) || $1 > maxTs[label]) {
        maxTs[label] = $1
      }
    }
    END {
      for (label in count) {
        split(values[label], raw, " ")
        n = 0
        delete sorted
        for (i in raw) {
          if (raw[i] != "") {
            n++
            sorted[n] = raw[i] + 0
          }
        }
        asort(sorted)
        p95Index = int((n * 95 + 99) / 100)
        if (p95Index < 1) {
          p95Index = 1
        }

        durationSec = (maxTs[label] - minTs[label]) / 1000.0
        if (durationSec <= 0) {
          durationSec = 1
        }

        avg = total[label] / count[label]
        p95 = sorted[p95Index]
        errRate = (error[label] + 0) * 100.0 / count[label]
        tps = count[label] / durationSec

        printf "%s\t%d\t%.2f\t%.2f\t%.2f\t%.2f\n", label, count[label], errRate, avg, p95, tps
      }
    }
  ' "${jtl_file}" | sort -k1,1
}

BEFORE_SUMMARY="$(summarize_jtl "${BEFORE_JTL}")"
AFTER_SUMMARY="$(summarize_jtl "${AFTER_JTL}")"
API_DIFF="$(
  awk -F'\t' '
    NR==FNR {
      bSamples[$1]=$2
      bError[$1]=$3
      bAvg[$1]=$4
      bP95[$1]=$5
      bTps[$1]=$6
      next
    }
    {
      label=$1
      aSamples=$2
      aError=$3
      aAvg=$4
      aP95=$5
      aTps=$6

      bs=(label in bSamples) ? bSamples[label] : "NaN"
      be=(label in bError) ? bError[label] : "NaN"
      ba=(label in bAvg) ? bAvg[label] : "NaN"
      bp=(label in bP95) ? bP95[label] : "NaN"
      bt=(label in bTps) ? bTps[label] : "NaN"

      dAvg="NaN"; dP95="NaN"; dErr="NaN"; dTps="NaN"
      if (ba != "NaN" && aAvg != "NaN" && ba != 0) dAvg=((aAvg - ba) / ba) * 100.0
      if (bp != "NaN" && aP95 != "NaN" && bp != 0) dP95=((aP95 - bp) / bp) * 100.0
      if (be != "NaN" && aError != "NaN") dErr=(aError - be)
      if (bt != "NaN" && aTps != "NaN" && bt != 0) dTps=((aTps - bt) / bt) * 100.0

      printf "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t", label, bs, aSamples, be, aError, ba, aAvg, bp, aP95
      if (dAvg == "NaN") printf "NaN\t"; else printf "%.2f\t", dAvg
      if (dP95 == "NaN") printf "NaN\t"; else printf "%.2f\t", dP95
      if (dErr == "NaN") printf "NaN\t"; else printf "%.2f\t", dErr
      if (dTps == "NaN") printf "NaN\t"; else printf "%.2f\t", dTps
      printf "%s\t%s\n", bt, aTps
    }
  ' <(printf '%s\n' "${BEFORE_SUMMARY}") <(printf '%s\n' "${AFTER_SUMMARY}") | sort -k1,1
)"

METRICS_DIFF=""
if [[ -f "${BEFORE_METRICS_TSV}" && -f "${AFTER_METRICS_TSV}" ]]; then
  METRICS_DIFF="$(
    awk -F'\t' '
      NR==FNR { beforeAvg[$1]=$2; beforeMax[$1]=$3; next }
      {
        metric=$1
        afterAvg=$2
        afterMax=$3
        bAvg=(metric in beforeAvg) ? beforeAvg[metric] : "NaN"
        bMax=(metric in beforeMax) ? beforeMax[metric] : "NaN"

        dAvg="NaN"
        dMax="NaN"
        if (bAvg != "NaN" && afterAvg != "NaN" && bAvg != 0) {
          dAvg=((afterAvg - bAvg) / bAvg) * 100.0
        }
        if (bMax != "NaN" && afterMax != "NaN" && bMax != 0) {
          dMax=((afterMax - bMax) / bMax) * 100.0
        }

        printf "%s\t%s\t%s\t%s\t%s\t", metric, bAvg, afterAvg, bMax, afterMax
        if (dAvg == "NaN") {
          printf "NaN\t"
        } else {
          printf "%.2f\t", dAvg
        }
        if (dMax == "NaN") {
          printf "NaN\n"
        } else {
          printf "%.2f\n", dMax
        }
      }
    ' "${BEFORE_METRICS_TSV}" "${AFTER_METRICS_TSV}" | sort -k1,1
  )"
fi

{
  echo "# Phase1 Virtual Thread Benchmark Summary"
  echo
  echo "- run_id: ${RUN_ID}"
  echo "- generated_at: $(date '+%Y-%m-%d %H:%M:%S')"
  echo
  echo "## Before"
  echo
  echo "| label | samples | error% | avg(ms) | p95(ms) | tps |"
  echo "|---|---:|---:|---:|---:|---:|"
  while IFS=$'\t' read -r label samples error avg p95 tps; do
    [[ -z "${label:-}" ]] && continue
    printf "| %s | %s | %s | %s | %s | %s |\n" "$label" "$samples" "$error" "$avg" "$p95" "$tps"
  done <<< "${BEFORE_SUMMARY}"
  echo
  echo "## After"
  echo
  echo "| label | samples | error% | avg(ms) | p95(ms) | tps |"
  echo "|---|---:|---:|---:|---:|---:|"
  while IFS=$'\t' read -r label samples error avg p95 tps; do
    [[ -z "${label:-}" ]] && continue
    printf "| %s | %s | %s | %s | %s | %s |\n" "$label" "$samples" "$error" "$avg" "$p95" "$tps"
  done <<< "${AFTER_SUMMARY}"

  if [[ -n "${METRICS_DIFF}" ]]; then
    echo
    echo "## Runtime Metrics (Before vs After)"
    echo
    echo "| metric | before_avg | after_avg | before_max | after_max | delta_avg(%) | delta_max(%) |"
    echo "|---|---:|---:|---:|---:|---:|---:|"
    while IFS=$'\t' read -r metric bAvg aAvg bMax aMax dAvg dMax; do
      [[ -z "${metric:-}" ]] && continue
      printf "| %s | %s | %s | %s | %s | %s | %s |\n" "$metric" "$bAvg" "$aAvg" "$bMax" "$aMax" "$dAvg" "$dMax"
    done <<< "${METRICS_DIFF}"
  fi

  echo
  echo "## API Delta (Before vs After)"
  echo
  echo "| label | before_avg(ms) | after_avg(ms) | delta_avg(%) | before_p95(ms) | after_p95(ms) | delta_p95(%) | before_error(%) | after_error(%) | delta_error(p) | before_tps | after_tps | delta_tps(%) |"
  echo "|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|"
  while IFS=$'\t' read -r label bSamples aSamples bError aError bAvg aAvg bP95 aP95 dAvg dP95 dErr dTps bTps aTps; do
    [[ -z "${label:-}" ]] && continue
    printf "| %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s |\n" \
      "$label" "$bAvg" "$aAvg" "$dAvg" "$bP95" "$aP95" "$dP95" "$bError" "$aError" "$dErr" "$bTps" "$aTps" "$dTps"
  done <<< "${API_DIFF}"
} > "${SUMMARY_MD}"

{
  echo "label,before_samples,after_samples,before_error_pct,after_error_pct,before_avg_ms,after_avg_ms,before_p95_ms,after_p95_ms,delta_avg_pct,delta_p95_pct,delta_error_point,delta_tps_pct,before_tps,after_tps"
  while IFS=$'\t' read -r label bSamples aSamples bError aError bAvg aAvg bP95 aP95 dAvg dP95 dErr dTps bTps aTps; do
    [[ -z "${label:-}" ]] && continue
    printf "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n" \
      "$label" "$bSamples" "$aSamples" "$bError" "$aError" "$bAvg" "$aAvg" "$bP95" "$aP95" "$dAvg" "$dP95" "$dErr" "$dTps" "$bTps" "$aTps"
  done <<< "${API_DIFF}"
} > "${API_DELTA_CSV}"

{
  echo "metric,before_avg,after_avg,before_max,after_max,delta_avg_pct,delta_max_pct"
  while IFS=$'\t' read -r metric bAvg aAvg bMax aMax dAvg dMax; do
    [[ -z "${metric:-}" ]] && continue
    printf "%s,%s,%s,%s,%s,%s,%s\n" "$metric" "$bAvg" "$aAvg" "$bMax" "$aMax" "$dAvg" "$dMax"
  done <<< "${METRICS_DIFF}"
} > "${RUNTIME_DELTA_CSV}"

echo "comparison summary generated: ${SUMMARY_MD}"
echo "api delta csv generated: ${API_DELTA_CSV}"
echo "runtime delta csv generated: ${RUNTIME_DELTA_CSV}"
