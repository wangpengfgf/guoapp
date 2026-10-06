import argparse
import os
import shutil
import subprocess
import sys
from pathlib import Path

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(
    description='构建短剧TV 双端播放器（android-native）：复用 native/ 站源核心，产物兼容 Android 6 及以上。'
)
parser.add_argument('--abi', action='append', choices=['arm64-v8a', 'armeabi-v7a', 'x86_64'])
parser.add_argument('--api-level', type=int, default=23,
                    help='站源核心编译目标 API，默认 23 以兼容 Android 6')
parser.add_argument('--skip-gradle', action='store_true', help='只构建站源核心，不打包 APK')
options = parser.parse_args()

environment = os.environ.copy()
environment.setdefault('GOPROXY', 'https://goproxy.cn,direct')
environment.setdefault('GOSUMDB', 'off')
abi_args = [item for abi in options.abi or ['arm64-v8a', 'armeabi-v7a']
            for item in ['--abi', abi]]

subprocess.run([sys.executable, str(root / 'scripts' / 'build_native.py'),
                '--platform', 'android',
                '--jni-root', 'android-native/app/src/main/jniLibs',
                '--api-level', str(options.api_level),
                '--all-sources', *abi_args],
               cwd=root, env=environment, check=True)
if options.skip_gradle:
    raise SystemExit(0)

gradle = shutil.which('gradle')
if not gradle:
    raise SystemExit('请先安装 Gradle 8.9 并加入 PATH，或在 CI 中使用 gradle/actions/setup-gradle。')
subprocess.run([gradle, '--no-daemon', '--console=plain',
                '-p', str(root / 'android-native'), ':app:assembleRelease'],
               cwd=root, env=environment, check=True)

dist = root / 'dist' / 'android-native'
dist.mkdir(parents=True, exist_ok=True)
outputs = sorted((root / 'android-native' / 'app' / 'build' / 'outputs' / 'apk' / 'release').glob('*.apk'))
if not outputs:
    raise SystemExit('未找到打包产物，请检查 Gradle 输出。')
for apk in outputs:
    shutil.copy2(apk, dist / apk.name)
    print('已生成 ' + str((dist / apk.name).relative_to(root)), flush=True)
