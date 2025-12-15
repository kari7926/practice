[app]
# (1) 앱 이름과 패키지 설정
title = My Review App
package.name = myreviewapp
package.domain = org.test
source.dir = .
source.include_exts = py,png,jpg,kv,atlas

# (2) 버전 및 요구사항
version = 0.1
requirements = python3,kivy

# (3) 안드로이드 설정
orientation = portrait
fullscreen = 0
android.permissions = INTERNET
android.api = 31
android.minapi = 21
android.archs = arm64-v8a, armeabi-v7a
p4a.branch = release-2022.12.20

[buildozer]
log_level = 2
warn_on_root = 1
