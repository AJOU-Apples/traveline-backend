# 테스트 코드 문서

여행 계획 API의 모든 기능에 대한 포괄적인 테스트 코드입니다.

## 📋 테스트 구조

```
src/test/java/org/apples/travelinebackend/
├── controller/
│   └── TravelPlanControllerTest.java          # Controller 단위 테스트
├── service/
│   └── TravelPlanServiceTest.java             # Service 단위 테스트
├── repository/
│   └── TravelPlanRepositoryTest.java          # Repository 테스트
├── mapper/
│   └── TravelPlanMapperTest.java              # Mapper 테스트
└── integration/
    └── TravelPlanIntegrationTest.java         # 통합 테스트
```

## 🧪 테스트 종류

### 1. Controller 테스트 (TravelPlanControllerTest)
**테스트 방식**: `@WebMvcTest` + MockMvc

**테스트 케이스** (총 14개):
- ✅ POST - 여행 계획 생성 성공
- ✅ POST - 필수 필드 누락 시 실패
- ✅ POST - 참가자 수 0 이하 시 실패
- ✅ POST - 장소 정보 포함 여행 계획 생성
- ✅ GET - 모든 여행 계획 조회 성공
- ✅ GET - 빈 목록 반환
- ✅ GET - 특정 여행 계획 조회 성공
- ✅ GET - 존재하지 않는 계획 조회 시 404
- ✅ PUT - 여행 계획 수정 성공
- ✅ PUT - 존재하지 않는 계획 수정 시 404
- ✅ DELETE - 여행 계획 삭제 성공
- ✅ DELETE - 존재하지 않는 계획 삭제 시 404

**주요 검증 항목**:
- HTTP 상태 코드
- 응답 JSON 구조 및 값
- 유효성 검증 동작
- 예외 처리

### 2. Service 테스트 (TravelPlanServiceTest)
**테스트 방식**: `@ExtendWith(MockitoExtension.class)` + Mockito

**테스트 케이스** (총 10개):
- ✅ 여행 계획 생성 성공
- ✅ 여행 계획 생성 - 일차 및 장소 포함
- ✅ 모든 여행 계획 조회
- ✅ 특정 여행 계획 조회 성공
- ✅ 존재하지 않는 여행 계획 조회 시 예외 발생
- ✅ 여행 계획 수정 성공
- ✅ 여행 계획 수정 - 일차 데이터 전체 교체
- ✅ 존재하지 않는 여행 계획 수정 시 예외 발생
- ✅ 여행 계획 삭제 성공
- ✅ 존재하지 않는 여행 계획 삭제 시 예외 발생

**주요 검증 항목**:
- 비즈니스 로직 정확성
- Repository 호출 횟수
- 예외 처리
- ArgumentCaptor를 통한 파라미터 검증

### 3. Repository 테스트 (TravelPlanRepositoryTest)
**테스트 방식**: `@DataJpaTest` + H2 인메모리 DB

**테스트 케이스** (총 10개):
- ✅ 여행 계획 저장 및 조회
- ✅ 여행 계획과 일차 정보 함께 저장 (Cascade)
- ✅ 여행 계획, 일차, 장소 모두 함께 저장 (Cascade)
- ✅ findAllWithDays - 모든 여행 계획과 일차 정보 조회
- ✅ findByIdWithDaysAndPlaces - 특정 여행 계획의 모든 정보 조회
- ✅ 여행 계획 삭제 시 일차와 장소도 함께 삭제 (Cascade)
- ✅ 여행 계획 업데이트 - updatedAt 자동 갱신
- ✅ 일차는 dayNumber 순서로 정렬
- ✅ 장소는 displayOrder 순서로 정렬

**주요 검증 항목**:
- 데이터 저장/조회 정확성
- Cascade 동작
- Timestamp 자동 생성
- @OrderBy 정렬 동작
- JOIN FETCH 최적화

### 4. Mapper 테스트 (TravelPlanMapperTest)
**테스트 방식**: 순수 단위 테스트

**테스트 케이스** (총 8개):
- ✅ TravelPlan Entity를 DTO로 변환
- ✅ TravelDay Entity를 DTO로 변환
- ✅ Place Entity를 DTO로 변환
- ✅ TravelPlan with Days를 DTO로 변환
- ✅ TravelDay with Places를 DTO로 변환
- ✅ null Entity는 null DTO 반환
- ✅ 전체 계층 구조 변환

**주요 검증 항목**:
- Entity → DTO 변환 정확성
- 중첩 구조 변환
- null 처리
- 날짜 형식 변환

### 5. 통합 테스트 (TravelPlanIntegrationTest)
**테스트 방식**: `@SpringBootTest` + MockMvc + 실제 DB

**테스트 케이스** (총 6개):
- ✅ 전체 시나리오: 생성 → 조회 → 수정 → 삭제
- ✅ 장소 정보 포함 여행 계획 전체 시나리오
- ✅ 여러 여행 계획 생성 후 목록 조회
- ✅ 일차 데이터 업데이트 테스트
- ✅ 유효성 검증 실패 테스트

**주요 검증 항목**:
- 엔드투엔드 시나리오
- 실제 데이터베이스 동작
- 여러 API 호출 조합
- 데이터 정합성

## 🚀 테스트 실행 방법

### 전체 테스트 실행
```bash
./gradlew test
```

### 특정 테스트 클래스 실행
```bash
# Controller 테스트만 실행
./gradlew test --tests TravelPlanControllerTest

# Service 테스트만 실행
./gradlew test --tests TravelPlanServiceTest

# Repository 테스트만 실행
./gradlew test --tests TravelPlanRepositoryTest

# 통합 테스트만 실행
./gradlew test --tests TravelPlanIntegrationTest
```

### 테스트 리포트 확인
```bash
./gradlew test
# 리포트 위치: build/reports/tests/test/index.html
```

## 📊 테스트 커버리지

### 주요 커버리지
- **Controller**: 14개 테스트
- **Service**: 10개 테스트  
- **Repository**: 10개 테스트
- **Mapper**: 8개 테스트
- **Integration**: 6개 테스트

**총 48개의 테스트 케이스**

### 테스트하는 기능
✅ CRUD 작업 (생성, 조회, 수정, 삭제)
✅ 유효성 검증
✅ 예외 처리
✅ Cascade 동작
✅ 정렬 기능
✅ N+1 문제 방지
✅ 타임스탬프 자동 생성
✅ Entity ↔ DTO 변환
✅ 엔드투엔드 시나리오

## 🔧 테스트 기술 스택

### 프레임워크 & 라이브러리
- **JUnit 5**: 테스트 프레임워크
- **Mockito**: Mocking 프레임워크
- **MockMvc**: Spring MVC 테스트
- **AssertJ**: Fluent assertion 라이브러리
- **Spring Boot Test**: 통합 테스트 지원
- **H2 Database**: 인메모리 테스트 DB

### 테스트 어노테이션
- `@WebMvcTest`: Controller 레이어 테스트
- `@DataJpaTest`: Repository 레이어 테스트
- `@SpringBootTest`: 통합 테스트
- `@ExtendWith(MockitoExtension.class)`: Mockito 확장
- `@MockBean`: Mock 객체 주입
- `@Autowired`: 실제 Bean 주입

## 📝 테스트 코드 예제

### Controller 테스트 예제
```java
@Test
@DisplayName("POST /api/travel-plans - 여행 계획 생성 성공")
void createTravelPlan_Success() throws Exception {
    // given
    CreateTravelPlanRequest request = CreateTravelPlanRequest.builder()
            .title("도쿄 여행")
            .destination("도쿄")
            .startDate("2024.11.20")
            .endDate("2024.11.23")
            .participants(2)
            .build();

    // when & then
    mockMvc.perform(post("/api/travel-plans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("도쿄 여행"));
}
```

### Service 테스트 예제
```java
@Test
@DisplayName("여행 계획 생성 성공")
void createTravelPlan_Success() {
    // given
    CreateTravelPlanRequest request = ...
    when(travelPlanRepository.save(any())).thenReturn(savedPlan);
    
    // when
    TravelPlanDto result = travelPlanService.createTravelPlan(request);
    
    // then
    assertThat(result.getTitle()).isEqualTo("도쿄 여행");
    verify(travelPlanRepository, times(1)).save(any());
}
```

### Repository 테스트 예제
```java
@Test
@DisplayName("여행 계획과 일차 정보 함께 저장 (Cascade)")
void saveTravelPlanWithDays_Cascade() {
    // given
    TravelPlan travelPlan = ...
    travelPlan.addDay(day1);
    
    // when
    TravelPlan savedPlan = travelPlanRepository.save(travelPlan);
    
    // then
    assertThat(savedPlan.getDays()).hasSize(1);
}
```

### 통합 테스트 예제
```java
@Test
@DisplayName("전체 시나리오: 생성 -> 조회 -> 수정 -> 삭제")
void fullScenario_CreateReadUpdateDelete() throws Exception {
    // 1. 생성
    MvcResult createResult = mockMvc.perform(post("/api/travel-plans")...)
            .andExpect(status().isCreated())
            .andReturn();
    
    // 2. 조회
    mockMvc.perform(get("/api/travel-plans/{id}", planId))
            .andExpect(status().isOk());
    
    // 3. 수정
    mockMvc.perform(put("/api/travel-plans/{id}", planId)...)
            .andExpect(status().isOk());
    
    // 4. 삭제
    mockMvc.perform(delete("/api/travel-plans/{id}", planId))
            .andExpect(status().isNoContent());
}
```

## ✅ 테스트 베스트 프랙티스

### 1. Given-When-Then 패턴
모든 테스트는 명확한 3단계 구조를 따릅니다:
- **Given**: 테스트 데이터 준비
- **When**: 테스트할 동작 실행
- **Then**: 결과 검증

### 2. 명확한 테스트 이름
- `@DisplayName`을 사용하여 한글로 명확한 설명
- 메서드명은 `테스트대상_시나리오_예상결과` 형식

### 3. 독립적인 테스트
- 각 테스트는 독립적으로 실행 가능
- `@AfterEach`로 테스트 데이터 정리
- 테스트 간 순서 의존성 없음

### 4. 적절한 Assertion
- AssertJ를 사용한 가독성 높은 assertion
- 예외 케이스도 적극적으로 테스트

### 5. Mock 최소화
- Repository 테스트는 실제 DB 사용
- 통합 테스트는 Mock 없이 전체 스택 테스트

## 🎯 테스트 실행 결과 예시

```
TravelPlanControllerTest
  ✓ POST /api/travel-plans - 여행 계획 생성 성공
  ✓ POST /api/travel-plans - 필수 필드 누락 시 실패
  ✓ GET /api/travel-plans - 모든 여행 계획 조회 성공
  ✓ GET /api/travel-plans/{planId} - 특정 여행 계획 조회 성공
  ✓ PUT /api/travel-plans/{planId} - 여행 계획 수정 성공
  ✓ DELETE /api/travel-plans/{planId} - 여행 계획 삭제 성공
  ... (총 14개)

TravelPlanServiceTest
  ✓ 여행 계획 생성 성공
  ✓ 모든 여행 계획 조회
  ✓ 특정 여행 계획 조회 성공
  ... (총 10개)

TravelPlanRepositoryTest
  ✓ 여행 계획 저장 및 조회
  ✓ 여행 계획과 일차 정보 함께 저장 (Cascade)
  ... (총 10개)

총 48개 테스트 - 모두 통과 ✅
```

## 🔍 CI/CD 통합

이 테스트들은 CI/CD 파이프라인에 쉽게 통합할 수 있습니다:

```yaml
# GitHub Actions 예시
- name: Run Tests
  run: ./gradlew test
  
- name: Publish Test Report
  uses: mikepenz/action-junit-report@v3
  if: always()
  with:
    report_paths: '**/build/test-results/test/TEST-*.xml'
```

## 📚 추가 참고사항

### 테스트 DB 설정
Repository와 통합 테스트는 H2 인메모리 DB를 사용하며, 각 테스트 후 자동으로 초기화됩니다:

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

### Mock vs Real
- **Controller 테스트**: Service를 Mock으로 사용
- **Service 테스트**: Repository를 Mock으로 사용  
- **Repository 테스트**: 실제 DB 사용 (H2)
- **통합 테스트**: 모든 레이어 실제 사용

이를 통해 각 레이어를 독립적으로 테스트하면서도, 통합 테스트로 전체 동작을 검증합니다.

