# 개발환경 세팅 가이드

본 문서는 백엔드 프로젝트의 로컬 개발 환경 세팅을 위한 가이드입니다.
Mac과 Windows 환경 모두 기입해놨습니다.
현재 브랜치 전략, 깃 컨벤션, Github Actions는 빠져있는 상태인 점을 감안해주시기 바랍니다.

## 1. 사전 준비

프로젝트를 실행하기 전에 아래 도구들이 설치되어 있어야 합니다.

1. **Java 21 (Eclipse Temurin)**
    * 버전: `21.0.10` 권장 (본인 OS의 아키텍처에 맞는 버전 설치, Mac M1/M2/M3의 경우 `aarch64`, Windows/Intel Mac의 경우 `x64`)
    * [Adoptium 다운로드 페이지](https://adoptium.net/temurin/releases/?version=21), IntelliJ를 쓰신다면 내부에서 설치 가능합니다.
2. **Docker Desktop**
    * 데이터베이스(PostgreSQL) 및 Redis 실행을 위해 필수입니다.
    * [Docker Desktop 다운로드](https://www.docker.com/products/docker-desktop)
3. **Git**

---

## 2. 환경 변수 설정

프로젝트 루트 디렉토리(backend 폴더 아래)에 `.env` 파일을 생성해야 합니다.

1. `env.example` 파일을 복사하여 `.env` 파일을 생성합니다.
    * **Mac/Linux:**
      ```bash
      cp env.example .env
      ```
    * **Windows (명령 프롬프트):**
      ```cmd
      copy env.example .env
      ```
    * **Windows (PowerShell):**
      ```powershell
      Copy-Item env.example -Destination .env
      ```

2. 생성된 `.env` 파일을 열고 팀 내에서 공유된 실제 환경 변수 값을 기입합니다. 현재는 DB 부분만 "자료실_개발자" 방에 올려두겠습니다.
    * `DB_NAME`, `DB_USER`, `DB_PASSWORD` (로컬 Docker 테스트용으로 자유롭게 설정 가능)
    * `GCP_*` 및 `GOOGLE_APPLICATION_CREDENTIALS` (GCP 서비스 계정 키 파일 경로)
    * `NAVER_*` (네이버 API 키)

> ⚠️ **주의:** `.env` 파일과 서비스 계정 JSON 파일(Google Credentials)은 절대 Git에 커밋하지 마세요! (`.gitignore`에 포함시켜놓은 상태입니다.)

---

## 3. 도커 기반 DB 세팅

Docker Compose를 사용하여 PostgreSQL(pgvector 18)과 Redis(8-alpine)를 세팅합니다.

1. Docker Desktop이 실행 중인지 확인합니다.
2. 터미널(또는 명령 프롬프트)을 열고 프로젝트 루트 디렉토리에서 아래 명령어를 실행합니다.

```bash
docker compose up -d
```
3. 이후 실행 시 .env 파일이 제대로 적용이 되지 않은 경우에는 IntelliJ 기준 Run -> Edit Configurations -> ![스크린샷 2026-03-11 오후 1.23.00.png](../../../../../../var/folders/l0/xv0psp7d5p15mjzcc53cxqfc0000gn/T/TemporaryItems/NSIRD_screencaptureui_moJGgC/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202026-03-11%20%EC%98%A4%ED%9B%84%201.23.00.png)
다음과 같이 세팅해주시면 됩니다.