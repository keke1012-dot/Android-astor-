#!/usr/bin/env python3
"""Verify an APK and reject a signature different from the durable unified key."""
import argparse
from pathlib import Path
import re
import subprocess

parser=argparse.ArgumentParser()
parser.add_argument("apk",type=Path)
parser.add_argument("--apksigner-jar",required=True,type=Path)
parser.add_argument("--java",default="java")
args=parser.parse_args()
expected=(Path(__file__).resolve().parents[1]/"security/UNIFIED_CERTIFICATE_SHA256.txt").read_text().strip()
result=subprocess.run([args.java,"-jar",str(args.apksigner_jar),"verify","--verbose","--print-certs",str(args.apk)],capture_output=True,text=True)
if result.returncode:
    raise SystemExit("APK signature verification failed")
digests=re.findall(r"certificate SHA-256 digest: ([0-9a-fA-F]+)",result.stdout)
if digests != [expected]:
    raise SystemExit("APK does not use the durable ASTOR unified signing key")
print("PASS: valid APK signed with the durable ASTOR unified key")
