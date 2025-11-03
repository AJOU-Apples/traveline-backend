# Travel Plan API Documentation

여행 계획 관리를 위한 RESTful API입니다.

## Base URL
```
http://localhost:8080/api
```

## API Endpoints

### 1. 여행 계획 생성
**POST** `/api/travel-plans`

여행 계획을 생성합니다.

#### Request Body
```json
{
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
          "memo": "입장료 1,200엔",
          "latitude": 35.6585805,
          "longitude": 139.7454329
        }
      ]
    }
  ]
}
```

#### Response (201 Created)
```json
{
  "id": 1,
  "title": "도쿄 여행",
  "destination": "도쿄",
  "startDate": "2024.11.20",
  "endDate": "2024.11.23",
  "participants": 2,
  "days": [
    {
      "id": 1,
      "dayNumber": 1,
      "date": "2024-11-20",
      "displayDate": "11월 20일(수)",
      "places": [
        {
          "id": 1,
          "name": "도쿄 타워",
          "address": "4 Chome-2-8 Shibakoen, Minato City, Tokyo",
          "time": "14:00",
          "memo": "입장료 1,200엔",
          "latitude": 35.6585805,
          "longitude": 139.7454329
        }
      ]
    }
  ]
}
```

### 2. 모든 여행 계획 조회
**GET** `/api/travel-plans`

저장된 모든 여행 계획을 조회합니다.

#### Response (200 OK)
```json
[
  {
    "id": 1,
    "title": "도쿄 여행",
    "destination": "도쿄",
    "startDate": "2024.11.20",
    "endDate": "2024.11.23",
    "participants": 2,
    "days": [...]
  },
  {
    "id": 2,
    "title": "파리 여행",
    "destination": "파리",
    "startDate": "2024.12.01",
    "endDate": "2024.12.05",
    "participants": 3,
    "days": [...]
  }
]
```

### 3. 특정 여행 계획 조회
**GET** `/api/travel-plans/{planId}`

특정 여행 계획의 상세 정보를 조회합니다.

#### Path Parameters
- `planId` (Long): 여행 계획 ID

#### Response (200 OK)
```json
{
  "id": 1,
  "title": "도쿄 여행",
  "destination": "도쿄",
  "startDate": "2024.11.20",
  "endDate": "2024.11.23",
  "participants": 2,
  "days": [
    {
      "id": 1,
      "dayNumber": 1,
      "date": "2024-11-20",
      "displayDate": "11월 20일(수)",
      "places": [...]
    }
  ]
}
```

#### Error Response (404 Not Found)
```json
{
  "timestamp": "2024-11-03T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "여행 계획을 찾을 수 없습니다. ID: 999"
}
```

### 4. 여행 계획 수정
**PUT** `/api/travel-plans/{planId}`

여행 계획을 수정합니다. 일부 필드만 수정할 수도 있고, 전체를 수정할 수도 있습니다.

#### Path Parameters
- `planId` (Long): 여행 계획 ID

#### Request Body
```json
{
  "title": "도쿄 여행 (수정됨)",
  "participants": 3,
  "days": [
    {
      "dayNumber": 1,
      "date": "2024-11-20",
      "displayDate": "11월 20일(수)",
      "places": [
        {
          "name": "도쿄 타워",
          "address": "4 Chome-2-8 Shibakoen, Minato City, Tokyo",
          "time": "15:00",
          "latitude": 35.6585805,
          "longitude": 139.7454329
        }
      ]
    }
  ]
}
```

#### Response (200 OK)
```json
{
  "id": 1,
  "title": "도쿄 여행 (수정됨)",
  "destination": "도쿄",
  "startDate": "2024.11.20",
  "endDate": "2024.11.23",
  "participants": 3,
  "days": [...]
}
```

### 5. 여행 계획 삭제
**DELETE** `/api/travel-plans/{planId}`

여행 계획을 삭제합니다.

#### Path Parameters
- `planId` (Long): 여행 계획 ID

#### Response (204 No Content)
응답 본문 없음

#### Error Response (404 Not Found)
```json
{
  "timestamp": "2024-11-03T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "여행 계획을 찾을 수 없습니다. ID: 999"
}
```

## 데이터 모델

### TravelPlan (여행 계획)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| id | Long | - | 자동 생성 ID |
| title | String | O | 여행 제목 |
| destination | String | O | 목적지 |
| startDate | String | O | 시작일 (YYYY.MM.DD) |
| endDate | String | O | 종료일 (YYYY.MM.DD) |
| participants | Integer | O | 참가자 수 (최소 1) |
| days | List<TravelDay> | X | 일차별 정보 |

### TravelDay (여행 일차)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| id | Long | - | 자동 생성 ID |
| dayNumber | Integer | O | 일차 번호 |
| date | String | O | 날짜 (YYYY-MM-DD) |
| displayDate | String | O | 표시용 날짜 (예: "11월 20일(수)") |
| places | List<Place> | X | 장소 목록 |

### Place (장소)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| id | Long | - | 자동 생성 ID |
| name | String | O | 장소 이름 |
| address | String | X | 주소 |
| time | String | X | 방문 시간 |
| memo | String | X | 메모 |
| latitude | Double | X | 위도 |
| longitude | Double | X | 경도 |

## 에러 처리

API는 다음과 같은 표준 HTTP 상태 코드를 사용합니다:

- `200 OK`: 요청 성공
- `201 Created`: 리소스 생성 성공
- `204 No Content`: 요청 성공 (응답 본문 없음)
- `400 Bad Request`: 잘못된 요청 (유효성 검증 실패)
- `404 Not Found`: 리소스를 찾을 수 없음
- `500 Internal Server Error`: 서버 오류

### 에러 응답 형식
```json
{
  "timestamp": "2024-11-03T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "입력값 검증 실패",
  "validationErrors": {
    "title": "제목은 필수입니다",
    "participants": "참가자는 최소 1명 이상이어야 합니다"
  }
}
```

## 예제 사용법

### cURL 예제

#### 여행 계획 생성
```bash
curl -X POST http://localhost:8080/api/travel-plans \
  -H "Content-Type: application/json" \
  -d '{
    "title": "도쿄 여행",
    "destination": "도쿄",
    "startDate": "2024.11.20",
    "endDate": "2024.11.23",
    "participants": 2,
    "days": []
  }'
```

#### 모든 여행 계획 조회
```bash
curl -X GET http://localhost:8080/api/travel-plans
```

#### 특정 여행 계획 조회
```bash
curl -X GET http://localhost:8080/api/travel-plans/1
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

## 데이터베이스 설정

프로젝트는 PostgreSQL을 사용합니다. `application.yml`에서 데이터베이스 연결 정보를 확인하세요.

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    username: postgres
    url: jdbc:postgresql://localhost:5432/traveline_test_db
    password: '1234'
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

테이블은 애플리케이션 시작 시 자동으로 생성됩니다 (ddl-auto: update).

