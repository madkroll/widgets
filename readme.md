# Description
REST API providing concurrent CRUD operations over repository of widgets.
Services listing widgets return widgets ordered by Z-index field.

Features:
- concurrent implementation of widget repository
- exposing REST API
- input validation
- rate limiting
- listing supports pagination
- unit testing
- integration testing (Spring MVC tests)

# Technology stack
- Spring Boot 2
- Java 11
- Lombok
- Bucket4j (rate limiting)
- AssertJ
- JUnit
- Maven

# Solution
## Repository
### ConcurrentHashMap with Read-Write locks
Scenario: we don't have any specific performance/latency requirements and having blocking operations over widget repository is OK.
List operation is called less often than other operations.

This is a quick-win solution. The price is that some operations are blocking.
For instance, creating a new widget or updating an existing one leads to shifting upwards all elements on that z-index and higher.
So until shifting is completed - none of other requests can list widgets. Also, only one shifting in a time is allowed.

Pros:
- simple
- thread-safe
- fast non-blocking operations: get, delete 
- single collection, no need to sync operations performed on multiple maps/sets/lists

Cons:
- slow list operation (performs sorting on each call), pagination does not reduce complexity
- slow blocking operations when in parallelism: list, insert, update

### ConcurrentHashMap with ConcurrentSkipListMap
