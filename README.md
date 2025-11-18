# swagger-to-zod

OpenAPI 3(Swagger) 문서를 기반으로 TypeScript `zod` 스키마와 타입 정의 코드를 생성하는 Java 21 + Spring Boot 기반 라이브러리입니다.

- OpenAPI 3 문서의 `schema`를 Zod 스키마로 변환
- `z.infer`를 이용한 TypeScript 타입 정의 자동 생성
- `allOf`, `anyOf`, `oneOf`, `array`, `enum` 등 다양한 타입 처리
- 스키마 의존성 정렬, 의존 관계 해결, 캐시 관리 유틸 포함

---

## 설치 및 빌드

프로젝트 루트에서 Gradle 명령어로 빌드합니다.

```bash
./gradlew clean build
```

빌드가 완료되면 `build/libs` 아래에 `swaggerToZod-<version>.jar` 파일이 생성됩니다.

**필수 요구 사항**

- Java 21 이상
- Gradle 8.x
- (선택) Spring Boot 애플리케이션 환경에서 함께 사용 가능

---

## 사용 방법

이 라이브러리는 크게 두 단계를 통해 Zod 코드를 생성합니다.

1. OpenAPI 3 문서를 읽어 모델을 구성 (`OpenApi3ToZodConvert`)
2. 읽어온 모델로부터 Zod 코드 및 타입 정보를 생성 (`ZodGenerator`)

### 1. OpenAPI 3 문서 로드

```java
import com.ncj.zod.covert.OpenApi3ToZodConvert;

// 예시: 원격 또는 로컬 OpenAPI 문서 URL
String openApiUrl = "https://example.com/openapi.yml";

OpenApi3ToZodConvert converter = new OpenApi3ToZodConvert(openApiUrl);

// 파싱된 OpenAPI 3 모델과 핸들러 매니저에 접근
var openApi3 = converter.getOpenApi3();
var handlerManager = converter.getTypeHandlerManager();
```

### 2. Zod 코드 생성

```java
import com.ncj.zod.generator.ZodGenerator;

ZodGenerator generator = new ZodGenerator(openApi3, handlerManager);

// 1) 전체 스키마를 하나의 TypeScript 코드 문자열로 생성
String zodCode = generator.generateZodSchemas();

// 2) ref 이름별 Zod 코드 Map 생성
var schemaMap = generator.generateZodSchemaMapByRefNames();

// 3) 변환된 스키마 원본 Map 조회
var convertedSchemas = generator.getConvertedSchemas();
```

`generateZodSchemas()`는 아래와 같이 Zod 스키마와 타입 정의를 포함한 TypeScript 코드를 생성합니다.

```ts
import { z } from 'zod';

export const userSchema = z.object({
  // ...
});

export type UserType = z.infer<typeof userSchema>;
```

---

## 주요 패키지 구조

- `com.ncj.zod.covert`
  - `OpenApi3ToZodConvert` : OpenAPI 3 문서를 URL로부터 읽고 기본 컨텍스트를 구성
  - `handler.*` : `string`, `number`, `boolean`, `array`, `enum`, `oneOf`, `anyOf`, `allOf` 등 타입별 Zod 변환 핸들러
- `com.ncj.zod.generator`
  - `ZodGenerator` : Zod 스키마 및 타입 코드 생성, ref 이름별 코드 Map 생성
  - `ZodData` : 생성된 스키마/맵 데이터를 묶어 관리하는 DTO
- `com.ncj.zod.manager`
  - `ZodTypeHandlerManager` : OpenAPI `Schema` → Zod 스키마 변환 관리
  - `ZodSchemaCacheManager` : 스키마 캐시 및 중복 변환 방지
- `com.ncj.zod.resolver`
  - `ZodSchemaDependencyResolver` : 스키마 의존 관계를 분석하고 코드 생성 순서를 해결
- `com.ncj.zod.sort`
  - `SchemaDependencySorter`, `ZodSchemaSort` : 의존성 기반 스키마 정렬
- `com.ncj.zod.utils`
  - `CommonUtils` : 이름/문자열 처리 유틸(대소문자 변환 등)
- `com.ncj.zod.format`
  - `ZodFormatter`, `ZodFormatOptions` : Zod 코드 포맷 옵션 및 포맷팅 유틸

---

## 테스트 실행

아래 명령어로 테스트를 실행할 수 있습니다.

```bash
./gradlew test
```

`SpringSwagerToZodApplicationTests`를 통해 Spring Boot 컨텍스트 로딩 여부를 확인합니다.

---

<img width="1433" height="1054" alt="image" src="https://github.com/user-attachments/assets/f0a95356-0f41-4898-859a-0f501d777d1e" />

