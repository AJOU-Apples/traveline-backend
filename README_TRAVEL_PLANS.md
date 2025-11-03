# 여행 계획 API 구현 완료

## 📋 개요
Traveline 프론트엔드와 연동되는 여행 계획 관리 REST API가 구현되었습니다.

## 🎯 구현된 API 엔드포인트

### 1. POST `/api/travel-plans`
- 새로운 여행 계획 생성
- Request Body: 여행 제목, 목적지, 날짜, 참가자, 일차별 정보
- Response: 201 Created + 생성된 여행 계획 정보

### 2. GET `/api/travel-plans`
- 모든 여행 계획 조회
- Response: 200 OK + 여행 계획 목록

### 3. GET `/api/travel-plans/{planId}`
- 특정 여행 계획 상세 조회
- Response: 200 OK + 여행 계획 상세 정보
- Error: 404 Not Found (존재하지 않는 계획)

### 4. PUT `/api/travel-plans/{planId}`
- 여행 계획 수정
- Request Body: 수정할 필드 (부분 업데이트 가능)
- Response: 200 OK + 수정된 여행 계획 정보

### 5. DELETE `/api/travel-plans/{planId}`
- 여행 계획 삭제
- Response: 204 No Content
- Error: 404 Not Found (존재하지 않는 계획)

## 📦 프로젝트 구조

```
src/main/java/org/apples/travelinebackend/
├── config/
│   └── WebConfig.java                    # CORS 설정
├── controller/
│   └── TravelPlanController.java         # REST API 엔드포인트
├── dto/
│   ├── CreateTravelPlanRequest.java      # 생성 요청 DTO
│   ├── UpdateTravelPlanRequest.java      # 수정 요청 DTO
│   ├── TravelPlanDto.java                # 여행 계획 응답 DTO
│   ├── TravelDayDto.java                 # 일차 정보 DTO
│   └── PlaceDto.java                     # 장소 정보 DTO
├── entity/
│   ├── TravelPlan.java                   # 여행 계획 엔티티
│   ├── TravelDay.java                    # 일차 엔티티
│   └── Place.java                        # 장소 엔티티
├── exception/
│   ├── GlobalExceptionHandler.java       # 전역 예외 처리
│   └── ErrorResponse.java                # 에러 응답 DTO
├── mapper/
│   └── TravelPlanMapper.java             # Entity ↔ DTO 변환
├── repository/
│   ├── TravelPlanRepository.java         # 여행 계획 Repository
│   ├── TravelDayRepository.java          # 일차 Repository
│   └── PlaceRepository.java              # 장소 Repository
└── service/
    └── TravelPlanService.java            # 비즈니스 로직
```

## 🔧 주요 기능

### 1. Entity 관계 설계
- **TravelPlan (1) → (N) TravelDay (1) → (N) Place**
- Cascade 설정으로 자동 생성/삭제
- orphanRemoval로 고아 객체 자동 제거
- @OrderBy로 정렬 보장

### 2. 데이터 검증
- Jakarta Validation 사용
- @NotBlank, @NotNull, @Min 등의 어노테이션
- 커스텀 에러 메시지

### 3. 예외 처리
- GlobalExceptionHandler로 통합 예외 처리
- 유효성 검증 실패 (400)
- 리소스 없음 (404)
- 서버 오류 (500)

### 4. CORS 설정
- 모든 origin 허용 (개발 환경)
- GET, POST, PUT, DELETE, OPTIONS 메서드 지원

### 5. N+1 문제 해결
- @Query + JOIN FETCH로 최적화
- 한 번의 쿼리로 연관 데이터 조회

## 🚀 사용 방법

### 1. 데이터베이스 설정
PostgreSQL 데이터베이스가 필요합니다:
```sql
CREATE DATABASE traveline_test_db;
```

`application.yml`에서 데이터베이스 연결 정보를 확인하세요.

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

또는 IDE에서 `TravelineBackendApplication` 실행

### 3. API 테스트

#### 여행 계획 생성 예제
```bash
curl -X POST http://localhost:8080/api/travel-plans \
  -H "Content-Type: application/json" \
  -d '{
    "title": "도쿄 여행",
    "destination": "도쿄",
    "startDate": "2024.11.20",
    "endDate": "2024.11.23",
    "participants": 2,
    "days": [
      {
        "dayNumber": 1,
        "date": "2024-11-20",
        "displayDate": "11월 20일(수)",
        "places": [
          {
            "name": "도쿄 타워",
            "address": "4 Chome-2-8 Shibakoen, Minato City, Tokyo",
            "time": "14:00",
            "latitude": 35.6585805,
            "longitude": 139.7454329
          }
        ]
      }
    ]
  }'
```

#### 모든 여행 계획 조회
```bash
curl http://localhost:8080/api/travel-plans
```

#### 특정 여행 계획 조회
```bash
curl http://localhost:8080/api/travel-plans/1
```

#### 여행 계획 수정
```bash
curl -X PUT http://localhost:8080/api/travel-plans/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "도쿄 여행 (수정됨)",
    "participants": 3
  }'
```

#### 여행 계획 삭제
```bash
curl -X DELETE http://localhost:8080/api/travel-plans/1
```

## 📊 데이터 모델

### TravelPlan (여행 계획)
```json
{
  "id": 1,
  "title": "도쿄 여행",
  "destination": "도쿄",
  "startDate": "2024.11.20",
  "endDate": "2024.11.23",
  "participants": 2,
  "days": [...]
}
```

### TravelDay (일차 정보)
```json
{
  "id": 1,
  "dayNumber": 1,
  "date": "2024-11-20",
  "displayDate": "11월 20일(수)",
  "places": [...]
}
```

### Place (장소)
```json
{
  "id": 1,
  "name": "도쿄 타워",
  "address": "4 Chome-2-8 Shibakoen, Minato City, Tokyo",
  "time": "14:00",
  "memo": "입장료 1,200엔",
  "latitude": 35.6585805,
  "longitude": 139.7454329
}
```

## 🔍 프론트엔드 연동 참고사항

### 1. 날짜 형식
- **startDate/endDate**: `YYYY.MM.DD` (예: "2024.11.20")
- **date**: `YYYY-MM-DD` (예: "2024-11-20")
- **displayDate**: 한글 표시용 (예: "11월 20일(수)")

### 2. ID 필드
- 생성 시 `id` 필드는 보내지 않음 (서버에서 자동 생성)
- 수정 시 하위 객체의 `id`도 생략 가능 (새로 생성됨)

### 3. 부분 업데이트
PUT 요청 시 변경하고 싶은 필드만 보내면 됩니다:
```json
{
  "title": "새로운 제목",
  "participants": 3
}
```

### 4. 일차 및 장소 관리
- 일차는 `dayNumber` 순서로 자동 정렬
- 장소는 `displayOrder` 순서로 자동 정렬
- Cascade로 인해 여행 계획 삭제 시 모든 하위 데이터 자동 삭제

## ⚙️ 기술 스택
- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **Jakarta Validation**
- **Java 17**

## 📝 추가 문서
자세한 API 문서는 `API_DOCUMENTATION.md` 파일을 참고하세요.

## ✅ 체크리스트
- [x] Entity 클래스 생성 (TravelPlan, TravelDay, Place)
- [x] DTO 클래스 생성 (Request/Response)
- [x] Repository 인터페이스 생성
- [x] Service 레이어 구현
- [x] Controller 구현 (5개 엔드포인트)
- [x] 전역 예외 처리
- [x] CORS 설정
- [x] N+1 문제 최적화
- [x] 데이터 검증
- [x] API 문서 작성

## 🎉 완료!
모든 API가 구현되었고, 프론트엔드와 연동할 준비가 완료되었습니다.

