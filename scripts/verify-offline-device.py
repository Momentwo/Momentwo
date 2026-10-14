#!/usr/bin/env python3
"""Run the installed isolated test APK, restore airplane mode, and verify a real process boundary."""
import argparse
import pathlib
import re
import subprocess

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--adb', default='adb')
parser.add_argument('--serial', required=True)
parser.add_argument('--package', default='cord.eoeo.momentwo.migrationcheck')
parser.add_argument('--fresh', action='store_true', help='Clear only the isolated target before testing')
parser.add_argument('--logs', default='/tmp/momentwo-offline-integration')
args = parser.parse_args()
if args.package != 'cord.eoeo.momentwo.migrationcheck':
    parser.error('Only the isolated migrationcheck package is allowed; the original app is preserved')
adb = [args.adb, '-s', args.serial]
logs = pathlib.Path(args.logs)
logs.mkdir(parents=True, exist_ok=True)


def shell(*command):
    return subprocess.run(adb + ['shell', *command], capture_output=True, text=True, check=True).stdout.strip()


def instrument(name, classes, extra=()):
    command = adb + ['shell', 'am', 'instrument', '-w', '-e', 'class', classes, *extra,
                     args.package + '.test/androidx.test.runner.AndroidJUnitRunner']
    result = subprocess.run(command, capture_output=True, text=True, check=True)
    output = result.stdout + result.stderr
    (logs / (name + '.log')).write_text(output)
    success = re.search(r'OK \((\d+) tests?\)', output)
    if not success or 'FAILURES!!!' in output or 'INSTRUMENTATION_FAILED' in output:
        raise RuntimeError(f'{name} failed; inspect {logs / (name + ".log")}')
    print(f'{name}: {success.group(1)} tests passed', flush=True)
    return int(success.group(1))


previous = shell('cmd', 'connectivity', 'airplane-mode')
if previous not in ('enabled', 'disabled'):
    raise RuntimeError('Cannot determine original airplane mode; device unchanged')
try:
    shell('cmd', 'connectivity', 'airplane-mode', 'enable')
    if shell('cmd', 'connectivity', 'airplane-mode') != 'enabled':
        raise RuntimeError('Airplane mode could not be enabled')
    print('Airplane mode enabled; original app preserved', flush=True)
    if args.fresh:
        if shell('pm', 'clear', args.package) != 'Success':
            raise RuntimeError('Could not clear isolated target')
    source = pathlib.Path(__file__).resolve().parents[1] / 'app/src/androidTest/java/cord/eoeo/momentwo'
    classes = ','.join('cord.eoeo.momentwo.' + path.stem for path in sorted(source.glob('*Test.kt'))
                       if path.stem != 'GuestProcessPersistenceSmokeTest')
    count = instrument('suite', classes)
    staged = 'cord.eoeo.momentwo.GuestProcessPersistenceSmokeTest'
    count += instrument('process-seed', staged, ('-e', 'persistenceStage', 'seed'))
    shell('am', 'force-stop', args.package)
    count += instrument('process-verify', staged, ('-e', 'persistenceStage', 'verify'))
    print(f'Total: {count} tests passed across the suite and two separate processes', flush=True)
finally:
    shell('cmd', 'connectivity', 'airplane-mode', 'enable' if previous == 'enabled' else 'disable')
    print('Airplane mode restored:', shell('cmd', 'connectivity', 'airplane-mode'), flush=True)
