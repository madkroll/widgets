# Description
REST API providing concurrent CRUD operations over repository of widgets.
Services listing widgets return widgets ordered by Z-index field.

Features:
- concurrent-friendly implementation of widget repository
- exposing REST API
- input validation
- rate limiting
- listing supports pagination
- unit testing
- integration testing (Spring MVC tests)

# Technology stack
- Spring Boot 2
- Java 11
- Maven
- Lombok
- Bucket4j (rate limiting)
- AssertJ
- JUnit
- Project Reactor (generate async parallel load for integration test)

# Setup
# Build and running unit tests
```shell script
cd $PROJECT_DIR
mvn clean install
```

# Run integration tests
There are several integration tests:
- Spring MVC
- Repository test (takes 1 minute) to prove there are no dead-locks when executing concurrently
```shell script
cd $PROJECT_DIR 
mvn verify -P integration
```

# Start application
To run application as jar file:
```shell script
cd $PROJECT_DIR
chmod +x widgets-web/target/widgets-web-1.0.0-SNAPSHOT.jar
java -jar widgets-web/target/widgets-web-1.0.0-SNAPSHOT.jar
```

# Request API
Create widget:
```shell script
curl -v \
  -H "Content-Type:application/json; charset=utf-8" \
  -X POST \
  'http://localhost:8080/widgets/create' \
  -d '{"x":1, "y":2, "z":-1, "width":4, "height":5}'
```

Delete widget:
```shell script
curl -v \
  -X DELETE \
  "http://localhost:8080/widgets/delete/${WIDGET_ID}"
```

Update widget:
```shell script
curl -v \
  -H "Content-Type:application/json; charset=utf-8" \
  -X PUT \
  "http://localhost:8080/widgets/update/${WIDGET_ID}" \
  -d '{"x":100, "y":100, "z":100, "width":100, "height":100}'
```

Get widget:
```shell script
curl -v "http://localhost:8080/widgets/get/${WIDGET_ID}"
```

List 5 widgets starting from 10th:
```shell script
curl -v "http://localhost:8080/widgets/list?from=10&limit=5"
```

List all widgets:
```shell script
curl -v "http://localhost:8080/widgets/list"
```

# Solution
## Repository
### ConcurrentHashMap with Read-Write locks
Scenario: we don't have any specific performance/latency requirements and having some operations blocking over widget repository is OK.

Considering that majority of incoming requests are read-only (GET, LIST). And only minor part of requests modifies the state (CREATE, UPDATE, DELETE).

This is a quick-win solution. The price is that some operations are blocking.
For instance, CREATE or UPDATE may lead to shifting upwards all elements on that z-index and higher.
So until shifting is completed - none of other threads can list widgets. Also, only one shifting in a time is allowed.
However, operations accessing widgets by id - are not blocking and read value at any time.

To make sure update of widget is done atomically - widgets are implemented as immutable objects.

Pros:
- simple
- thread-safe
- fast non-blocking operations: get, delete 
- single collection, no need to sync operations performed on multiple maps/sets/lists

Cons:
- slow list operation (performs sorting on each call), pagination does not reduce complexity
- relatively slow blocking operations on high parallelism: list, insert, update

### Alternative proposal: non-blocking repository
Scenario: high level of parallelism, many write operations.

I have an idea in mind on how widget repository may be designed in order to avoid as much as possible blocking the access to widgets.

Here are main points:
- Have a widget-by-id registry.
Just a guess on possible implementation: `ConcurrentHashMap<String, LockedWidget<AtomicReference<Widget>>>`
Provides fast access by id. Also, allows to block access to each widget individually.
- Keep widgets sorted by Z-index in a non-blocking manner.
Again, idea in mind: `ConcurrentSkipListMap<Integer, String>` (String is widget-id, so no need to sync consistency between maps)
- Then, this should be possible to apply shifting one-by-one to all widgets sequentially starting from the highest down to inserted/updated element
- Safety is relatively secured by blocking operations on widget level (so the rest of widgets are not locked)
- Additionally, seems like having for short time old and new states together in a sorted map is not a critical problem.
LastUpdated field may help to leave the most recent element and ignore all previous versions added.
So this garbage can sit for some time without having any impact on customer experience, until it's cleaned or replaced. 

That's also true I may be missing some other concurrent scenarios at the moment, so not sure if such a concept is actually safe.
It requires more time to think of and to design it. 

And, of course, there are going to be race conditions between parallel shifts and widget modifications.
So customer experience will anyway differ from first blocking solution.
The question is - if it is acceptable or not for this particular product.
