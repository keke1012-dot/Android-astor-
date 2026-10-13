#!/usr/bin/env python3
"""Verify signed update identity/version and immutable native-library packaging."""
import argparse
import hashlib
import json
from pathlib import Path
import struct
import subprocess
import zipfile

PACKAGE = "fr.astor.hades.unified.test.unified029"

def manifest_metadata(apk):
    with zipfile.ZipFile(apk) as archive:
        data = archive.read("AndroidManifest.xml")
    def u16(offset): return struct.unpack_from("<H", data, offset)[0]
    def u32(offset): return struct.unpack_from("<I", data, offset)[0]
    if len(data) > 1_000_000 or u16(0) != 3 or u32(4) != len(data):
        raise ValueError("Invalid Android manifest")
    strings, attributes = [], {}
    offset = u16(2)
    while offset < len(data):
        kind, header, size = u16(offset), u16(offset + 2), u32(offset + 4)
        if size < header or size == 0 or offset + size > len(data):
            raise ValueError("Invalid XML chunk")
        if kind == 1:
            count, flags, start = u32(offset + 8), u32(offset + 16), u32(offset + 20)
            def length8(pos):
                value = data[pos]
                return (((value & 127) << 8) | data[pos + 1], pos + 2) if value & 128 else (value, pos + 1)
            def length16(pos):
                value = u16(pos)
                return (((value & 32767) << 16) | u16(pos + 2), pos + 4) if value & 32768 else (value, pos + 2)
            for index in range(count):
                pos = offset + start + u32(offset + header + 4 * index)
                if flags & 256:
                    _, pos = length8(pos)
                    length, pos = length8(pos)
                    strings.append(data[pos:pos + length].decode("utf-8"))
                else:
                    length, pos = length16(pos)
                    strings.append(data[pos:pos + 2 * length].decode("utf-16le"))
        elif kind == 0x102:
            ext = offset + 16
            element = strings[u32(ext + 4)]
            if element not in ("manifest", "application", "uses-sdk"):
                offset += size
                continue
            start, stride, count = u16(ext + 8), u16(ext + 10), u16(ext + 12)
            if stride < 20: raise ValueError("Invalid attribute size")
            for index in range(count):
                pos = ext + start + index * stride
                if pos + 20 > offset + size: raise ValueError("Invalid attribute bounds")
                name = strings[u32(pos + 4)]
                raw, value_type, value = u32(pos + 8), data[pos + 15], u32(pos + 16)
                if raw != 0xffffffff: value = strings[raw]
                elif value_type == 3: value = strings[value]
                elif value_type == 0x12: value = bool(value)
                key = (element, name)
                if key in attributes: raise ValueError("Duplicate manifest attribute")
                attributes[key] = value
        offset += size
    return {
        "package": attributes.get(("manifest", "package")),
        "versionCode": attributes.get(("manifest", "versionCode")),
        "versionName": attributes.get(("manifest", "versionName")),
        "minSdk": attributes.get(("uses-sdk", "minSdkVersion")),
        "debuggable": attributes.get(("application", "debuggable"), False),
        "allowBackup": attributes.get(("application", "allowBackup")),
        "cleartext": attributes.get(("application", "usesCleartextTraffic")),
    }

def verify(apk, previous, jar, java):
    checker = Path(__file__).with_name("verify_unified_signature.py")
    for path in (previous, apk):
        subprocess.run(["python3", str(checker), str(path), "--apksigner-jar", str(jar), "--java", java], check=True)
    old, new = manifest_metadata(previous), manifest_metadata(apk)
    if old["package"] != PACKAGE or new["package"] != PACKAGE:
        raise ValueError("Unified application identity changed")
    if not isinstance(new["versionCode"], int) or new["versionCode"] <= old["versionCode"]:
        raise ValueError("Update versionCode must increase")
    if new["minSdk"] != old["minSdk"] or new["debuggable"]:
        raise ValueError("SDK compatibility or release debugging changed")
    if new["allowBackup"] is not False or new["cleartext"] is not False:
        raise ValueError("Privacy protections changed")
    with zipfile.ZipFile(apk) as archive:
        libraries = [item for item in archive.infolist() if item.filename.startswith("lib/") and item.filename.endswith(".so")]
        if not libraries or any(not item.filename.startswith("lib/arm64-v8a/") for item in libraries):
            raise ValueError("Invalid native architecture")
        with open(apk, "rb") as source:
            for item in libraries:
                source.seek(item.header_offset + 26)
                name_size, extra_size = struct.unpack("<HH", source.read(4))
                data_start = item.header_offset + 30 + name_size + extra_size
                if item.compress_type != zipfile.ZIP_STORED or data_start % 16384:
                    raise ValueError("Native library is not aligned for 16 KiB pages")
    new["sha256"] = hashlib.sha256(Path(apk).read_bytes()).hexdigest()
    return new

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("apk", type=Path)
    parser.add_argument("--previous", required=True, type=Path)
    parser.add_argument("--apksigner-jar", required=True, type=Path)
    parser.add_argument("--java", default="java")
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    try:
        result = verify(args.apk, args.previous, args.apksigner_jar, args.java)
        encoded = json.dumps(result, ensure_ascii=False, indent=2) + "\n"
        if args.report: args.report.write_text(encoded)
        print("PASS: compatible signed update, increasing version, arm64 and 16 KiB alignment")
    except (ValueError, subprocess.CalledProcessError, OSError, zipfile.BadZipFile, struct.error) as error:
        raise SystemExit("Update verification failed: " + str(error))
