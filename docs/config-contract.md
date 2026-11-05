# Spring Config Contract 파일럿

## 목적

Spring Config Contract v0.1.0은 애플리케이션의 `@Value`, 지원되는 `@ConfigurationProperties`, `application*.yml` 설정 요구사항과 배포 입력의 환경 변수 키를 비교한다. 값은 비교하거나 결과에 출력하지 않는다. 누락된 필수 키는 `MISSING`, 애플리케이션에서 인식하지 못한 제공 키는 `UNUSED`로 보고하며 하나라도 있으면 task가 실패한다.

이 저장소에는 배포 전 설정 불일치를 탐지할 수 있는지 평가하기 위해 수동 검사로만 추가했다. `check`, `build`, CI workflow에는 연결하지 않았다.

## 실행

Java 21 환경에서 저장소 루트의 Gradle wrapper를 사용한다.

```shell
./gradlew configContractCheck
```

Windows에서는 다음 명령을 사용한다.

```powershell
.\gradlew.bat configContractCheck
```

task 설명은 다음 명령으로 확인할 수 있다.

```powershell
.\gradlew.bat help --task configContractCheck
```

## 입력과 프로필

- 배포 입력은 루트 `.env.example`만 사용한다.
- `activeProfiles`는 `prod`로 지정했다. `application.yml`의 기본값이 `prod`이고 `.env.example`도 운영 프로필의 환경 변수 구조를 나타내기 때문이다.
- 기본 설정과 `application-prod.yml`을 운영 시나리오로 평가한다. `application-dev.yml`은 개발 전용 FCM 입력처럼 계약이 다르므로 같은 결과로 취급하지 않고 이번 task 입력에서 제외한다.
- `docker-compose.yml`은 입력에서 제외한다. v0.1.0은 MariaDB, Redis, app을 구분하지 않고 모든 서비스의 `environment`를 합치며 app의 `env_file`을 지원하지 않아 현재 파일을 입력하면 유효한 애플리케이션 계약을 만들 수 없다.

## 검증 환경과 호환성 판단

- GOMS: Spring Boot 4.0.2, Kotlin 2.2.21, Java 21, Gradle 9.3.0
- Spring Config Contract: 0.1.0
- 플러그인 공식 검증 범위: Spring Boot 3.5.16, Java 21, 플러그인 저장소의 Gradle wrapper

Gradle 9.3.0에서 플러그인 해석, task 등록, Kotlin 컴파일, Java 21 bytecode 로딩과 일부 `@Value`·단순 `@ConfigurationProperties` 탐색은 실행됐다. class loading 또는 dependency 충돌은 발생하지 않았다.

이 결과만으로 Spring Boot 4 호환성을 확인했다고 볼 수 없다. 플러그인은 Spring Boot 3.5.16 API를 포함해 parent-first class loader로 탐색하고 Spring Boot 4.x를 지원 대상으로 명시하지 않는다. GOMS의 운영 프로필 파일도 아래 제한으로 제외되므로 현재 결과는 전체 설정 계약을 신뢰성 있게 나타내지 못한다.

## 실제 실행 결과

`configContractCheck`는 2건의 `MISSING`, 27건의 `UNUSED`, 6건의 진단을 출력하고 실패했다. secret 값은 출력되지 않았다.

### 오탐과 미지원 사례

- `application-prod.yml`은 프로필 전용 파일 안의 `spring.config.activate.on-profile`을 v0.1.0이 지원하지 않아 문서 전체가 제외됐다.
- 이 제외로 운영 파일의 환경 변수 alias를 발견하지 못해 `discord.deploy-env`, `internal.discord.secret` 2건이 `MISSING`으로 오탐됐다.
- 같은 원인으로 DB, Redis, Mail, JPA, AWS, 배포 환경과 내부 API secret 관련 `.env.example` 키 21건이 `UNUSED`로 오탐됐다.
- Springdoc 활성화 2건과 multipart 제한 2건은 dependency가 소비하는 Spring 설정이지만 애플리케이션 클래스 스캔 범위에서 발견되지 않아 `UNUSED`로 보고됐다.
- `JAVA_TOOL_OPTIONS`, `TZ` 2건은 각각 JVM과 컨테이너 운영 환경이 소비하므로 Spring 애플리케이션 설정 계약의 `UNUSED`가 아니다.
- `CorsProperties`의 `List`와 `S3Properties`의 중첩 타입 3개는 v0.1.0이 지원하지 않아 진단만 출력하고 탐색하지 않았다.
- `application.yml`의 `spring.profiles.active`는 플러그인이 입력에서 프로필을 추론하지 않으므로 미적용 진단이 출력됐다. 이 때문에 build script에서 `prod`를 명시했다.

현재 출력에서 실제 `MISSING` 또는 실제 `UNUSED`로 확정할 항목은 없었다. 이는 전체 계약이 정상이라는 의미가 아니라, 운영 프로필 제외와 지원 범위 때문에 task 결과로 실제 문제를 판별할 수 없다는 의미다.

## 알려진 공백

- 값의 형식, 타입, 빈 값과 실제 secret 유효성은 검사하지 않는다.
- Compose `env_file`과 서비스별 환경 구분을 검사하지 않는다.
- 중첩·collection·map `@ConfigurationProperties`를 검사하지 않는다.
- dependency가 제공하는 설정 metadata나 JVM·컨테이너 변수를 인식하지 않는다.
- 개발 프로필 계약은 별도 개발용 배포 입력이 없어 평가하지 않는다.

따라서 v0.1.0은 현재 GOMS에서 참고용 수동 분석으로만 유지한다. CI 필수 검사로 사용하려면 upstream에서 Spring Boot 4 호환성을 검증하고, 프로필 전용 파일의 activation 처리와 false positive를 제어할 서비스 선택 또는 allowlist가 제공된 뒤 다시 평가해야 한다.

## upstream 전달용 재현 요약

1. Spring Boot 4.0.2, Kotlin 2.2.21, Java 21, Gradle 9.3.0 프로젝트에 플러그인 v0.1.0을 적용한다.
2. `activeProfiles`에 `prod`, `dotenvExampleFiles`에 `.env.example`을 지정한다.
3. `application-prod.yml` 최상단 문서에 `spring.config.activate.on-profile: prod`와 환경 변수 placeholder를 둔다.
4. `configContractCheck`를 실행하면 해당 문서가 `UNSUPPORTED`로 제외되고, 문서의 alias가 연결하던 키가 `MISSING`과 `UNUSED`로 분리돼 보고된다.

기대 동작은 명시한 active profile에서 Spring Boot가 실제로 사용하는 프로필 문서의 placeholder를 계약에 포함하는 것이다. upstream Issue는 자동 생성하지 않는다.
