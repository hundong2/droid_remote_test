# Droid Remote Test

Android remote test tools with LLM - Kotlin (Ktor + Adam) implementation

## 개요 (Overview)

이 프로젝트는 Kotlin, Ktor 웹 프레임워크, 그리고 Adam 라이브러리를 사용하여 Android 디바이스를 원격으로 제어하고 모니터링할 수 있는 서버입니다.

This project is a server built with Kotlin, Ktor web framework, and the Adam library for remote control and monitoring of Android devices.

## 주요 기능 (Key Features)

### 1. 📱 디바이스 관리 (Device Management)
- 연결된 Android 디바이스 목록 조회
- 디바이스 속성 및 정보 확인

### 2. 📋 Logcat Flow (로그 수집)
- **실시간 Logcat 스트리밍**: WebSocket을 통한 실시간 로그 수집
- **최근 로그 조회**: HTTP API를 통한 최근 로그 가져오기
- **로그 초기화**: Logcat 버퍼 비우기

### 3. 🎮 명령 제어 (Command Control)
- **Shell 명령 실행**: 임의의 ADB shell 명령 실행
- **앱 설치/제거**: APK 설치 및 패키지 제거
- **입력 제어**: 
  - 텍스트 입력
  - 화면 터치 (tap)
  - 스와이프 동작
  - 키 이벤트 전송
- **시스템 제어**:
  - 디바이스 재부팅
  - 화면 정보 조회

## 기술 스택 (Tech Stack)

- **Kotlin**: JVM 기반 현대적인 프로그래밍 언어
- **Ktor**: 경량 비동기 웹 프레임워크
- **Adam**: Android Debug Bridge (ADB) 클라이언트 라이브러리
- **Kotlinx Coroutines**: 비동기 프로그래밍 및 Flow API
- **Kotlinx Serialization**: JSON 직렬화/역직렬화

## 요구사항 (Requirements)

- JDK 17 이상
- Android Debug Bridge (ADB) 설치 및 실행 중
- 연결된 Android 디바이스 또는 에뮬레이터

## 보안 주의사항 (Security Notes)

⚠️ **중요**: 이 서버는 개발 및 테스트 목적으로 설계되었습니다.

- 기본적으로 서버는 `0.0.0.0:8080`에 바인딩되어 네트워크에서 접근 가능합니다
- 프로덕션 환경에서는 인증 메커니즘을 추가하거나 방화벽으로 보호해야 합니다
- 로컬 개발 전용으로 사용하려면 Application.kt에서 `host = "127.0.0.1"`로 변경하세요
- CORS는 localhost와 127.0.0.1로만 제한되어 있습니다

⚠️ **Important**: This server is designed for development and testing purposes.

- By default, the server binds to `0.0.0.0:8080` and is accessible from the network
- For production use, add authentication mechanisms or protect with a firewall
- For local development only, change to `host = "127.0.0.1"` in Application.kt
- CORS is restricted to localhost and 127.0.0.1 only

## 설치 및 실행 (Installation & Running)

### 1. 프로젝트 빌드
```bash
./gradlew build
```

### 2. 서버 실행
```bash
./gradlew run
```

서버는 기본적으로 `http://0.0.0.0:8080`에서 실행됩니다.

### 3. ADB 서버 시작 (필요한 경우)
```bash
adb start-server
```

## API 사용법 (API Usage)

### 디바이스 목록 조회
```bash
GET http://localhost:8080/devices
```

응답 예시:
```json
[
  {
    "serial": "emulator-5554",
    "state": "DEVICE",
    "model": null,
    "product": null,
    "device": null,
    "transportId": null
  }
]
```

### 실시간 Logcat 스트리밍 (WebSocket)
```javascript
const ws = new WebSocket('ws://localhost:8080/logcat/emulator-5554');
ws.onmessage = (event) => {
  console.log('Log:', event.data);
};
```

### 최근 로그 조회
```bash
GET http://localhost:8080/logcat/{serial}/recent?lines=100
```

### Logcat 초기화
```bash
POST http://localhost:8080/logcat/{serial}/clear
```

### Shell 명령 실행
```bash
POST http://localhost:8080/command/{serial}
Content-Type: application/json

{
  "command": "pm list packages"
}
```

응답 예시:
```json
{
  "success": true,
  "output": "package:com.android.chrome\npackage:com.android.settings\n...",
  "error": null
}
```

### 텍스트 입력
```bash
POST http://localhost:8080/command/{serial}/input
Content-Type: application/x-www-form-urlencoded

text=Hello World
```

### 화면 터치
```bash
POST http://localhost:8080/command/{serial}/tap
Content-Type: application/x-www-form-urlencoded

x=500&y=1000
```

### 스와이프 동작
```bash
POST http://localhost:8080/command/{serial}/swipe
Content-Type: application/x-www-form-urlencoded

x1=500&y1=1000&x2=500&y2=500&duration=300
```

### 키 이벤트 전송
```bash
POST http://localhost:8080/command/{serial}/keyevent
Content-Type: application/x-www-form-urlencoded

keycode=4  # Back button
```

일반적인 키코드:
- 3: HOME
- 4: BACK
- 26: POWER
- 82: MENU

### 디바이스 속성 조회
```bash
GET http://localhost:8080/devices/{serial}/properties
```

## 프로젝트 구조 (Project Structure)

```
droid_remote_test/
├── build.gradle.kts          # Gradle 빌드 설정
├── settings.gradle.kts        # Gradle 프로젝트 설정
├── gradle.properties          # Gradle 속성
└── src/main/
    ├── kotlin/com/hundong2/droid/
    │   ├── Application.kt     # 메인 애플리케이션 및 Ktor 설정
    │   ├── DeviceManager.kt   # Adam을 사용한 디바이스 관리
    │   ├── DeviceRoutes.kt    # 디바이스 관련 API 엔드포인트
    │   ├── LogcatRoutes.kt    # Logcat 관련 API 엔드포인트
    │   └── CommandRoutes.kt   # 명령 실행 API 엔드포인트
    └── resources/
        └── logback.xml        # 로깅 설정
```

## 개발자 정보 (Developer Info)

- **Group**: com.hundong2.droid
- **Version**: 1.0.0
- **Kotlin Version**: 1.9.22
- **Ktor Version**: 2.3.7
- **Adam Version**: 0.5.4

## 라이선스 (License)

이 프로젝트의 라이선스는 LICENSE 파일을 참조하세요.

## 기여 (Contributing)

이슈 제출 및 Pull Request는 언제든지 환영합니다!

## 참고 자료 (References)

- [Ktor Documentation](https://ktor.io/)
- [Adam Library](https://github.com/Malinskiy/adam)
- [Android Debug Bridge (ADB)](https://developer.android.com/studio/command-line/adb)
