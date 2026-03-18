# VehicleSure Repository Layer - Complete Reference

## 📦 Overview

**Total Repositories**: 16  
**Pattern**: Spring Data JPA (extends `JpaRepository<T, Long>`)  
**Location**: `org.thehartford.willowshield.repository`  
**Status**: ✅ Complete

All repositories leverage Spring Data JPA for CRUD operations + custom query methods.

---

## 🏗️ Repository Details

### 1. **UserRepository**
**File**: `UserRepository.java`  
**Entity**: `User`

**Methods**:
- `findByUsername(String username)` → Optional<User>
- `findByEmail(String email)` → Optional<User>
- `findByRole(User.UserRole role)` → List<User>
- `findByIsActive(Boolean isActive)` → List<User>
- `findByUsernameOrEmail(String username, String email)` → Optional<User>

**Use Cases**:
- Authentication & login
- User profile lookup
- Role-based user filtering

---

### 2. **IndividualCricketerRepository**
**File**: `IndividualCricketerRepository.java`  
**Entity**: `IndividualCricketer`

**Methods**:
- `findByUserId(Long userId)` → Optional<IndividualCricketer>
- `findByCompetitionLevelAndPrimaryFormat(...)` → Optional<IndividualCricketer>

**Use Cases**:
- Athlete profile retrieval
- Risk assessment by competition level & format

---

### 3. **TeamRepository**
**File**: `TeamRepository.java`  
**Entity**: `Team`

**Methods**:
- `findByAdminUserId(Long adminUserId)` → Optional<Team>
- `findByCompetitionLevel(Team.CompetitionLevel competitionLevel)` → List<Team>
- `findByPrimaryFormat(Team.CricketFormat primaryFormat)` → List<Team>
- `findByTeamName(String teamName)` → Optional<Team>

**Use Cases**:
- Team admin lookup
- Filter teams by competition level
- Find teams by format

---

### 4. **PlayerRepository**
**File**: `PlayerRepository.java`  
**Entity**: `Player`

**Methods**:
- `findByTeamId(Long teamId)` → List<Player>
- `findByTeamIdAndPlayingRole(Long teamId, Player.PlayerRole playerRole)` → List<Player>
- `findByCompetitionLevel(Player.CompetitionLevel competitionLevel)` → List<Player>
- `findByFastBowlerFlagTrue()` → List<Player>
- `findByMajorSurgeryFlagTrue()` → List<Player>
- `findByInjuryHistoryCountGreaterThan(Integer count)` → List<Player>

**Use Cases**:
- Get all team members
- Filter by position (fast bowlers for high risk)
- Identify players with injury history
- Risk factor assessment

---

### 5. **UnderwriterProfileRepository**
**File**: `UnderwriterProfileRepository.java`  
**Entity**: `UnderwriterProfile`

**Methods**:
- `findByUserId(Long userId)` → Optional<UnderwriterProfile>
- `findBySpecialization(UnderwriterProfile.Specialization specialization)` → List<UnderwriterProfile>
- `findByActiveFlagTrue()` → List<UnderwriterProfile>
- `findBySpecializationAndActiveFlagTrue(...)` → List<UnderwriterProfile>

**Use Cases**:
- Get underwriter profile
- Find specialists (ATHLETE, TEAM, EVENT, INFRA)
- List active underwriters for assignment

---

### 6. **PolicyRepository**
**File**: `PolicyRepository.java`  
**Entity**: `Policy`

**Methods**:
- `findByPolicyName(String policyName)` → Optional<Policy>
- `findByPolicyType(Policy.PolicyType policyType)` → List<Policy>
- `findByIsActiveTrue()` → List<Policy>
- `findByPolicyTypeAndIsActiveTrue(Policy.PolicyType policyType)` → List<Policy>

**Use Cases**:
- Policy product lookup
- Get available policies by type (ATHLETE_CRICKET, TEAM_CRICKET, EVENT_CRICKET, INFRA_CRICKET)
- List active products for sale

---

### 7. **PolicySubscriptionRepository** ⭐ CORE
**File**: `PolicySubscriptionRepository.java`  
**Entity**: `PolicySubscription`

**Methods**:
- `findByUserId(Long userId)` → List<PolicySubscription>
- `findByStatus(PolicySubscription.SubscriptionStatus status)` → List<PolicySubscription>
- `findByUnderwriterId(Long underwriterId)` → List<PolicySubscription>
- `findByUnderwritingStatus(PolicySubscription.UnderwritingStatus underwritingStatus)` → List<PolicySubscription>
- `findByEventId(Long eventId)` → List<PolicySubscription>
- `findByInfrastructureId(Long infrastructureId)` → List<PolicySubscription>
- `findActiveSubscriptionsByDate(LocalDate currentDate)` → List<PolicySubscription> (custom query)
- `findExpiredSubscriptions(LocalDate currentDate)` → List<PolicySubscription> (custom query)
- `findByUserIdAndStatus(Long userId, SubscriptionStatus status)` → List<PolicySubscription>
- `countByStatus(SubscriptionStatus status)` → Long

**Use Cases**:
- Customer policy retrieval
- Underwriting workflow (pending, approved, rejected)
- Event-linked policies
- Infrastructure-linked policies
- Active/expired policy tracking
- Dashboard statistics

---

### 8. **TeamPlayerCoverageRepository**
**File**: `TeamPlayerCoverageRepository.java`  
**Entity**: `TeamPlayerCoverage`

**Methods**:
- `findByPolicySubscriptionId(Long policySubscriptionId)` → List<TeamPlayerCoverage>
- `findByPlayerId(Long playerId)` → List<TeamPlayerCoverage>
- `findByPolicySubscriptionIdAndPlayerId(Long policySubscriptionId, Long playerId)` → List<TeamPlayerCoverage>
- `deleteByPolicySubscriptionId(Long policySubscriptionId)` → void

**Use Cases**:
- Get coverage details for a policy
- Get all coverages for a player
- Calculate team exposure
- Cascade delete coverage on policy cancellation

---

### 9. **ClaimRepository**
**File**: `ClaimRepository.java`  
**Entity**: `Claim`

**Methods**:
- `findByPolicySubscriptionId(Long policySubscriptionId)` → List<Claim>
- `findByStatus(Claim.ClaimStatus status)` → List<Claim>
- `findByPlayerId(Long playerId)` → List<Claim>
- `findByAssignedOfficerId(Long assignedOfficerId)` → List<Claim>
- `findByClaimType(Claim.ClaimType claimType)` → List<Claim>
- `findByFraudFlagTrue()` → List<Claim>
- `findByInvestigationRequiredTrue()` → List<Claim>
- `findByStatusAndAssignedOfficerId(Claim.ClaimStatus status, Long assignedOfficerId)` → List<Claim>
- `countByStatus(Claim.ClaimStatus status)` → Long
- `countByFraudFlagTrue()` → Long

**Use Cases**:
- Get policy-related claims
- Filter claims by status (SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED)
- Assign claims to officers
- Fraud detection
- Investigation tracking
- Claim statistics

---

### 10. **PremiumPaymentRepository**
**File**: `PremiumPaymentRepository.java`  
**Entity**: `PremiumPayment`

**Methods**:
- `findByPolicySubscriptionId(Long policySubscriptionId)` → List<PremiumPayment>
- `findByStatus(PremiumPayment.PaymentStatus status)` → List<PremiumPayment>
- `findByDueDateBefore(LocalDate dueDate)` → List<PremiumPayment>
- `findByStatusAndDueDateBefore(PremiumPayment.PaymentStatus status, LocalDate dueDate)` → List<PremiumPayment>
- `findByPolicySubscriptionIdAndStatus(Long policySubscriptionId, PaymentStatus status)` → List<PremiumPayment>
- `countByStatus(PremiumPayment.PaymentStatus status)` → Long

**Use Cases**:
- Get payment schedule for a policy
- Identify overdue payments
- Track payment status (PENDING, PARTIAL, PAID, OVERDUE, FAILED)
- Billing reminders
- Payment reconciliation

---

### 11. **EventOrganizerRepository**
**File**: `EventOrganizerRepository.java`  
**Entity**: `EventOrganizer`

**Methods**:
- `findByUserId(Long userId)` → Optional<EventOrganizer>
- `findByOrganizationName(String organizationName)` → Optional<EventOrganizer>

**Use Cases**:
- Event organizer profile lookup
- Organization identification

---

### 12. **EventRepository**
**File**: `EventRepository.java`  
**Entity**: `Event`

**Methods**:
- `findByOrganizerId(Long organizerId)` → List<Event>
- `findByEventType(Event.EventType eventType)` → List<Event>
- `findEventsByDateRange(LocalDate fromDate, LocalDate toDate)` → List<Event> (custom query)
- `findOngoingEvents(LocalDate currentDate)` → List<Event> (custom query)
- `findUpcomingEvents(LocalDate currentDate)` → List<Event> (custom query)
- `findByEventName(String eventName)` → Optional<Event>
- `findByLocation(String location)` → List<Event>

**Use Cases**:
- Get organizer's events
- Filter events by type (LEAGUE, SERIES, LOCAL_TOURNAMENT)
- Event timeline filtering
- Event search

---

### 13. **EventRiskAssessmentRepository**
**File**: `EventRiskAssessmentRepository.java`  
**Entity**: `EventRiskAssessment`

**Methods**:
- `findByEventId(Long eventId)` → Optional<EventRiskAssessment>

**Use Cases**:
- Get risk assessment for event insurance
- Weather & cancellation risk lookup

---

### 14. **InfrastructureOwnerRepository**
**File**: `InfrastructureOwnerRepository.java`  
**Entity**: `InfrastructureOwner`

**Methods**:
- `findByUserId(Long userId)` → Optional<InfrastructureOwner>
- `findByOwnershipType(InfrastructureOwner.OwnershipType ownershipType)` → List<InfrastructureOwner>

**Use Cases**:
- Get infrastructure owner profile
- Filter owners by type (PRIVATE, GOVERNMENT)

---

### 15. **InfrastructureRepository**
**File**: `InfrastructureRepository.java`  
**Entity**: `Infrastructure`

**Methods**:
- `findByOwnerId(Long ownerId)` → List<Infrastructure>
- `findByType(Infrastructure.InfrastructureType type)` → List<Infrastructure>
- `findByName(String name)` → Optional<Infrastructure>
- `findByLocation(String location)` → List<Infrastructure>
- `findByTypeAndLocation(InfrastructureType type, String location)` → List<Infrastructure>
- `findByYearBuiltGreaterThan(Integer year)` → List<Infrastructure>

**Use Cases**:
- Get owner's facilities
- Filter by type (STADIUM, PRACTICE_GROUND, ACADEMY)
- Facility search
- Risk assessment by age

---

### 16. **InfrastructureRiskAssessmentRepository**
**File**: `InfrastructureRiskAssessmentRepository.java`  
**Entity**: `InfrastructureRiskAssessment`

**Methods**:
- `findByInfrastructureId(Long infrastructureId)` → Optional<InfrastructureRiskAssessment>

**Use Cases**:
- Get risk assessment for infrastructure insurance
- Fire, flood, earthquake, terrorism risk lookup

---

## 🔍 Query Patterns Used

### 1. **Derived Query Methods** (No @Query needed)
```java
// Spring auto-generates query from method name
findByUserId(Long userId)
findByStatus(Status status)
findByFastBowlerFlagTrue()
findByCreatedAtBetween(LocalDate start, LocalDate end)
```

### 2. **Custom JPQL Queries** (@Query)
```java
@Query("SELECT ps FROM PolicySubscription ps WHERE ps.startDate <= :currentDate AND ps.endDate >= :currentDate AND ps.status = 'ACTIVE'")
List<PolicySubscription> findActiveSubscriptionsByDate(@Param("currentDate") LocalDate currentDate);
```

### 3. **Aggregation Methods**
```java
Long countByStatus(Status status);
Long countByFraudFlagTrue();
```

### 4. **Batch Operations**
```java
void deleteByPolicySubscriptionId(Long policySubscriptionId);
```

---

## 📊 Repository Usage by Layer

```
┌──────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                          │
│                (Business Logic & Rules)                   │
└──────────────────────────────────────────────────────────┘
                          ↓
┌──────────────────────────────────────────────────────────┐
│                  REPOSITORY LAYER (16)                    │
│            (Data Access & Persistence)                   │
└──────────────────────────────────────────────────────────┘
                          ↓
┌──────────────────────────────────────────────────────────┐
│                    DATABASE (H2 / MySQL)                  │
│                    (Data Storage)                         │
└──────────────────────────────────────────────────────────┘
```

---

## 🔄 Data Flow Examples

### Example 1: Get Active Policies for User
```
Controller Request
    ↓
Service: getUserPolicies(userId)
    ↓
PolicySubscriptionRepository.findByUserIdAndStatus(userId, ACTIVE)
    ↓
Database Query
    ↓
Service: Add business logic (fees, formatting)
    ↓
Response DTO
```

### Example 2: Claim Processing Workflow
```
ClaimsService
    ↓
Get Claim → ClaimRepository.findById(claimId)
Get Subscription → PolicySubscriptionRepository.findById(subscriptionId)
Get Coverages → TeamPlayerCoverageRepository.findByPolicySubscriptionId(subscriptionId)
Get Payments → PremiumPaymentRepository.findByPolicySubscriptionId(subscriptionId)
    ↓
Apply Validation & Underwriting Logic
    ↓
Update Claim Status → ClaimRepository.save(claim)
```

### Example 3: Risk Assessment for Event Insurance
```
EventInsuranceService
    ↓
Get Event → EventRepository.findById(eventId)
Get Risk Assessment → EventRiskAssessmentRepository.findByEventId(eventId)
Get Organizer → EventOrganizerRepository.findByUserId(organizerId)
    ↓
Calculate Premium Based on Risk Scores
    ↓
Create PolicySubscription → PolicySubscriptionRepository.save(subscription)
```

---

## ✅ Implementation Features

### Automatic CRUD Operations
All repositories inherit from `JpaRepository<T, Long>`:
- `save(T entity)` → Create/Update
- `findById(Long id)` → Read
- `delete(T entity)` → Delete
- `deleteById(Long id)` → Delete by PK
- `findAll()` → Read all
- `saveAll(Iterable<T>)` → Bulk insert/update
- `deleteAll()` → Clear table

### Pagination & Sorting
Can be extended with `PagingAndSortingRepository` if needed:
```java
public interface PolicySubscriptionRepository extends 
    JpaRepository<PolicySubscription, Long>,
    PagingAndSortingRepository<PolicySubscription, Long>
```

### Transaction Management
Handled automatically by Spring Data JPA with `@Transactional` on Service layer.

---

## 🚀 Next Integration Steps

### 1. **Service Layer Creation**
Create services that use these repositories:
- `UserService`
- `PolicySubscriptionService` (core logic)
- `ClaimService`
- `UnderwritingService`
- `PremiumCalculationService`
- etc.

### 2. **Controller Layer**
Create REST controllers that call services:
```java
@RestController
@RequestMapping("/api/policies")
public class PolicyController {
    @Autowired
    private PolicySubscriptionService policyService;
    
    @GetMapping("/{userId}")
    public List<PolicyDTO> getUserPolicies(@PathVariable Long userId) {
        return policyService.getUserPolicies(userId);
    }
}
```

### 3. **DTO Layer**
Create request/response DTOs for API contracts.

### 4. **Exception Handling**
Create custom exceptions + global exception handler.

### 5. **Validation**
Add Bean Validation annotations to entities or DTOs.

---

## 📈 Performance Considerations

### Indexes Created (via Entity Annotations)
- UNIQUE(username), UNIQUE(email) - User lookups
- UNIQUE(user_id) - IndividualCricketer, Team, EventOrganizer, InfrastructureOwner, UnderwriterProfile
- INDEX(role) - User filtering
- INDEX(team_id) - Player lookups
- INDEX(policy_subscription_id) - Claim, Payment, Coverage lookups
- INDEX(status) - Status filtering
- INDEX(start_date, end_date) - Date range queries
- INDEX(user_id) - Policy lookups

### Query Optimization Tips
1. Use **batch loading** for related entities
2. Use **@Query with custom JPQL** for complex joins
3. Use **Pagination** for large result sets
4. Use **Lazy loading** for relationships (default in JPA)

---

## 📁 File Listing

```
repository/
├── UserRepository.java ✅
├── IndividualCricketerRepository.java ✅
├── TeamRepository.java ✅
├── PlayerRepository.java ✅
├── UnderwriterProfileRepository.java ✅
├── PolicyRepository.java ✅
├── PolicySubscriptionRepository.java ✅
├── TeamPlayerCoverageRepository.java ✅
├── ClaimRepository.java ✅
├── PremiumPaymentRepository.java ✅
├── EventOrganizerRepository.java ✅
├── EventRepository.java ✅
├── EventRiskAssessmentRepository.java ✅
├── InfrastructureOwnerRepository.java ✅
├── InfrastructureRepository.java ✅
└── InfrastructureRiskAssessmentRepository.java ✅
```

---

**Status**: ✅ All 16 repositories created and compiled successfully  
**Last Updated**: February 26, 2026  
**Architecture**: Spring Data JPA with Domain-Driven Design


