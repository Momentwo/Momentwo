#!/usr/bin/env python3
"""Small SDK smoke on an ephemeral Linux CI emulator; never runs on a developer device."""
import argparse
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import time


def run(*args, timeout=120, check=True):
    result = subprocess.run(args, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=timeout)
    if check and result.returncode:
        raise RuntimeError(f"Command failed ({result.returncode}): {args[0]}\n{result.stdout}")
    return result.stdout


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--api", type=int, required=True, choices=[29, 35, 36, 37])
    parser.add_argument("--apks", type=Path, required=True)
    args = parser.parse_args()
    if sys.platform != "linux" or os.environ.get("CI") != "true" or os.environ.get("GITHUB_ACTIONS") != "true":
        parser.error("Only an ephemeral GitHub Actions Linux runner is supported (no local/physical execution).")
    sdk = Path(os.environ.get("ANDROID_HOME") or os.environ["ANDROID_SDK_ROOT"])
    adb = str(sdk / "platform-tools/adb")
    serial = "emulator-5554"
    output = Path("build/reports/sdk-runtime")
    output.mkdir(parents=True, exist_ok=True)
    def shell(*parts, **kwargs):
        return run(adb, "-s", serial, "shell", *parts, **kwargs)
    def instrument(name, count, classes, *extra):
        result = shell("am", "instrument", "-w", "-r", "-e", "class", classes, *extra,
                       "cord.eoeo.momentwo.test/androidx.test.runner.AndroidJUnitRunner", timeout=300)
        (output / f"{name}.txt").write_text(result)
        print(result, flush=True)
        # am instrument can exit zero on assertion failure or instrumentation crash.
        completed = len(re.findall(r"^INSTRUMENTATION_STATUS_CODE: 0$", result, re.MULTILINE))
        if completed != count or not re.search(rf"^OK \({count} tests?\)$", result, re.MULTILINE) or "FAILURES!!!" in result:
            raise RuntimeError(f"{name}: expected {count} executed tests; see instrumentation output")
    def wait_boot():
        deadline = time.monotonic() + 300
        while time.monotonic() < deadline:
            if emulator.poll() is not None:
                raise RuntimeError("Emulator exited before boot; see emulator.log")
            if shell("getprop", "sys.boot_completed", check=False, timeout=15).strip() == "1":
                shell("input", "keyevent", "KEYCODE_WAKEUP")
                shell("wm", "dismiss-keyguard")
                return
            time.sleep(3)
        raise TimeoutError("Emulator did not boot within five minutes")
    def apk_at(relative, package):
        folder = args.apks / relative
        metadata = json.loads((folder / "output-metadata.json").read_text())
        if metadata["applicationId"] != package or len(metadata["elements"]) != 1:
            raise ValueError(f"Unexpected APK metadata at {folder}")
        apk = folder / metadata["elements"][0]["outputFile"]
        if not apk.is_file():
            raise FileNotFoundError(apk)
        return str(apk)
    app_apk = apk_at("debug", "cord.eoeo.momentwo")
    test_apk = apk_at("androidTest/debug", "cord.eoeo.momentwo.test")
    if not Path("/dev/kvm").exists() or not os.access("/dev/kvm", os.R_OK | os.W_OK):
        raise RuntimeError("KVM access is required; software CPU emulation is not supported")
    existing = run(adb, "devices")
    if re.search(r"^\S+\s+(device|offline|unauthorized)$", existing, re.MULTILINE):
        raise RuntimeError("Refusing to use a runner with an existing Android device")
    platform = "37.0" if args.api == 37 else str(args.api)
    image = f"system-images;android-{platform};google_apis;x86_64"
    avdmanager = shutil.which("avdmanager")
    if not avdmanager:
        raise RuntimeError("avdmanager is not on PATH")
    create = subprocess.run([avdmanager, "create", "avd", "--force", "--name", "sdk-smoke", "--package", image,
                             "--device", "pixel_6"], input="no\n", text=True, capture_output=True, timeout=60)
    if create.returncode:
        raise RuntimeError(create.stdout + create.stderr)
    log = (output / "emulator.log").open("w")
    emulator = subprocess.Popen([str(sdk / "emulator/emulator"), "-avd", "sdk-smoke", "-port", "5554",
                                 "-no-window", "-no-audio", "-no-boot-anim", "-no-snapshot", "-memory", "2048",
                                 "-cores", "2", "-gpu", "software"], stdout=log, stderr=subprocess.STDOUT)
    prefix = "cord.eoeo.momentwo."
    persistence = prefix + "GuestProcessPersistenceSmokeTest#processBoundaryPreservesOwnedFilesAndRecords"
    try:
        run(adb, "-s", serial, "wait-for-device", timeout=120)
        wait_boot()
        actual = shell("getprop", "ro.build.version.sdk").strip()
        if actual != str(args.api):
            raise RuntimeError(f"Expected API {args.api}, got {actual}")
        shell("settings", "put", "global", "window_animation_scale", "0")
        shell("settings", "put", "global", "transition_animation_scale", "0")
        shell("settings", "put", "global", "animator_duration_scale", "0")
        shell("settings", "put", "secure", "show_ime_with_hard_keyboard", "1")
        if args.api >= 35:
            shell("cmd", "overlay", "enable-exclusive", "--category", "com.android.internal.systemui.navbar.gestural")
        run(adb, "-s", serial, "install", "-r", app_apk)
        run(adb, "-s", serial, "install", "-r", test_apk)
        # Only this disposable emulator is disconnected; never change physical device settings.
        shell("svc", "wifi", "disable")
        shell("svc", "data", "disable")
        shell("cmd", "connectivity", "airplane-mode", "enable", check=False)
        # The instrumentation asserts no active transport; settings alone are not treated as proof.
        tests = [
            prefix + "GuestAlbumCrudSmokeTest#albumAndSubAlbumCanBeCreatedEditedReopenedAndDeletedWithoutApi",
            prefix + "GuestPhotoCrudSmokeTest#captionMemoFavoriteAndImmersiveWorkWithoutApiAndPersistAfterActivityRestart",
            prefix + "AppNavigationSmokeTest#loginSignUpAndBackUseFeatureGraph",
            prefix + "AppNavigationSmokeTest#systemBarsRestoreOnPauseAndDisposalAndReapplyOnResume",
        ]
        if args.api >= 35:
            tests.append(prefix + "SystemBackGestureSmokeTest#systemBackHidesImeBeforeLeavingCreateAlbumAndPreservesInput")
        instrument("offline-ui", len(tests), ",".join(tests), "-e", "requireOffline", "true")
        shell("cmd", "connectivity", "airplane-mode", "disable", check=False)
        shell("svc", "wifi", "enable")
        instrument("gallery-export", 1, prefix + "ImageLoaderCompatibilityTest#galleryExportPreservesOriginalDimensionsBeyondDefaultBitmapLimit", "-e", "requireOnline", "true")
        if args.api == 37:
            instrument("persistence-seed", 1, persistence, "-e", "persistenceStage", "seed")
            run(adb, "-s", serial, "reboot")
            run(adb, "-s", serial, "wait-for-device", timeout=120)
            wait_boot()
            instrument("persistence-verify", 1, persistence, "-e", "persistenceStage", "verify")
            shell("wm", "size", "1600x1200")
            shell("wm", "density", "200")
            shell("settings", "put", "system", "font_scale", "1.3")
            shell("settings", "put", "system", "accelerometer_rotation", "0")
            shell("settings", "put", "system", "user_rotation", "1")
            # The new activity must adapt to the larger display without an opt-out or orientation lock.
            instrument("large-screen", 2, ",".join([tests[1], tests[-1]]))
        (output / "result.json").write_text(json.dumps({"api": args.api, "status": "passed"}, indent=2) + "\n")
    finally:
        (output / "logcat.txt").write_text(run(adb, "-s", serial, "logcat", "-d", "-s", "AndroidRuntime:E", "TestRunner:I", check=False, timeout=30))
        run(adb, "-s", serial, "emu", "kill", check=False, timeout=15)
        try:
            emulator.wait(timeout=15)
        except subprocess.TimeoutExpired:
            emulator.kill()
            emulator.wait()
        log.close()


if __name__ == "__main__":
    main()
