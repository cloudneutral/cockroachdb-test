[![Java CI with Maven](https://github.com/cloudneutral/cockroachdb-test/actions/workflows/maven.yml/badge.svg)](https://github.com/cloudneutral/cockroachdb-test/actions/workflows/maven.yml)

# CockroachDB Test

<img align="left" src="logo.png" />

The goal of this Java library is to provide for embedded database integration tests against 
a local [CockroachDB](https://www.cockroachlabs.com/) instance running either in [single node](https://www.cockroachlabs.com/docs/stable/cockroach-start-single-node) 
mode or [demo](https://www.cockroachlabs.com/docs/stable/cockroach-demo) mode. It supports both [Junit 4](http://junit.org/junit4/) and [Junit 5](http://junit.org/junit5/).

Integration tests are more realistic alternative than local [mock](https://site.mockito.org/) or fake/stub 
unit tests. One typical choice for database-oriented integration tests is 
to use [H2](https://www.h2database.com/html/main.html) which a capable in-memory SQL database written in Java. 
The main limiting factor is that you are not really testing against CockroachDB 
but instead the dialect and semantics of H2.

CockroachDB is natively written in Go, and as such doesn't embed itself into a JVM instance.
A close alternative however, is to run a local and separate CockroachDB process that is 
controlled and governed by the integration test cycle. 

Using this test extension you can start with a clean machine, have the CockroachDB binary 
downloaded automatically, expanded and a local process started and initialized. At the
end of the test cycle, the inverse takes place where the process is stopped and all 
local files deleted. The downloaded binary can be cached and re-used between test runs
to speed things up.

## Disclaimer

This project is not supported by Cockroach Labs. Use of this library is entirely 
at your own risk and Cockroach Labs makes no guarantees or warranties about its operation.

See [MIT](LICENSE.txt) license for terms and conditions.

## Supported Platforms and Versions

* JUnit4
* JUnit5
* Spring3
* Linux, Mac and Windows
* CockroachDB Dedicated v23.1 or later
* Java17+ LTS SDK

# Getting Started

A quick getting started guide using CockroachDB integration tests.

## Maven Configuration

Add this dependency to your `pom.xml` file if you are using Junit5:

```xml
<dependency>
    <groupId>io.cockroachdb.test</groupId>
    <artifactId>cockroachdb-test-junit5</artifactId>
    <version>{version}</version>
</dependency>
```

Alternatively, if you are using Junit4:

```xml
<dependency>
    <groupId>io.cockroachdb.test</groupId>
    <artifactId>cockroachdb-test-junit4</artifactId>
    <version>{version}</version>
</dependency>
```

Then add the Maven repository to your `pom.xml` file (alternatively in Maven's [settings.xml](https://maven.apache.org/settings.html)):

```xml
<repository>
    <id>cockroachdb-test</id>
    <name>Maven Packages</name>
    <url>https://maven.pkg.github.com/cloudneutral/cockroachdb-test</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```

Next, you need to authenticate to GitHub Packages by creating a personal access token (classic)
that includes the `read:packages` scope. For more information, see [Authenticating to GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-to-github-packages).

Add your personal access token to the servers section in your [settings.xml](https://maven.apache.org/settings.html):

```xml
<server>
    <id>github</id>
    <username>your-github-name</username>
    <password>your-access-token</password>
</server>
```

Take note that the server and repository id's must be an exact match.

## JUnit5 Example

The highlights in the following example are the `@RegisterExtension` and `@Cockroach` annotations 
used to register and configure the CockroachDB extension. 

```java
@Cockroach(
        version = "v24.2.0",
        architecture = Cockroach.Architecture.amd64,
        command = Cockroach.Command.demo,
        demoFlags = @DemoFlags(global = true, nodes = 9)
)
public class CockroachJunit5Test {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @RegisterExtension
    public static CockroachExtension cockroachExtension =
            CockroachExtension.builder()
                    .withTestClass(CockroachJunit5Test.class)
                    .build();

    private CockroachDetails processDetails;

    public void setProcessDetails(CockroachDetails processDetails) {
        this.processDetails = processDetails;
    }

    @Test
    public void whenCockroachStarted_thenSayHelloAndWait() throws SQLException {
        Assertions.assertNotNull(processDetails);

        logger.info("Attempting connection to [{}] with credentials {}/{}",
                processDetails.getJdbcURL(),
                processDetails.getUser(),
                processDetails.getPassword());

        try (Connection db = DriverManager.getConnection(
                processDetails.getJdbcURL(),
                processDetails.getUser(),
                processDetails.getPassword());
             Statement s = db.createStatement();
             ResultSet rs = s.executeQuery("SELECT 1+1")) {
            Assertions.assertTrue(rs.next());
            Assertions.assertEquals(2, rs.getInt(1));
        }
    }
}
```

## Spring Boot Example

Add this dependency to your `pom.xml` file if you are using Spring 3.x with JUnit5:

```xml
<dependency>
    <groupId>io.cockroachdb.test</groupId>
    <artifactId>cockroachdb-test-spring3</artifactId>
    <version>{version}</version>
</dependency>
```

The CockroachDB process needs to be started before the datasource bean is initialized, which presents
a small dependency management challenge. For that reason we can't just use the test lifecycle events.
The JDBC connection URL `spring.datasource.url` for the embedded instance must also 
be injected to the datasource prior to creation.

This can all be handled by the `ApplicationEnvironmentPreparedListener` that listens to the 
`ApplicationEnvironmentPreparedEvent` application event at spring context startup time. To use it, 
simply add the `EmbeddedCockroachLoader` context loader to your integration test:

```java
@SpringBootTest(classes = DemoApp.class)
@ContextConfiguration(loader = EmbeddedCockroachLoader.class)
public class EmbeddedCockroachTest {
    
}
```

A more complete example:

```java
@Tag("integration-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@ContextConfiguration(loader = EmbeddedCockroachLoader.class)
@Cockroach(
        version = "v24.2.0",
        architecture = Cockroach.Architecture.arm64,
        command = Cockroach.Command.start_single_node,
        experimental = true,
        startFlags = @StartFlags(listenAddr = "localhost"),
        initSQL = {
                "SET CLUSTER SETTING kv.range_merge.queue_interval = '50ms'",
                "SET CLUSTER SETTING jobs.registry.interval.gc = '30s'",
                "SET CLUSTER SETTING jobs.registry.interval.cancel = '180s'",
                "SET CLUSTER SETTING jobs.retention_time = '15s'",
                "SET CLUSTER SETTING sql.stats.automatic_collection.enabled = false",
                "SET CLUSTER SETTING kv.range_split.by_load_merge_delay = '5s'",
                "ALTER RANGE default CONFIGURE ZONE USING \"gc.ttlseconds\" = 600",
                "ALTER DATABASE system CONFIGURE ZONE USING \"gc.ttlseconds\" = 600"
        }
)
public class EmbeddedCockroachTest {
    @Autowired
    private DataSource dataSource;

    @Test
    public void whenContextStarted_thenPrintDatabaseVersion() {
        logger.info("Connected to: {}",
                new JdbcTemplate(dataSource)
                        .queryForObject("select version()", String.class));
    }
}
```

## Getting Help

### Reporting Issues
                                                             
CockroachDB Test uses [GitHub](https://github.com/cloudneutral/cockroachdb-test/issues) as issue tracking system to record bugs and feature requests. 
If you want to raise an issue, please follow the recommendations below:

* Before you log a bug, please search the [issue tracker](https://github.com/cloudneutral/cockroachdb-test/issues) 
to see if someone has already reported the problem.
* If the issue doesn’t exist already, [create a new issue](https://github.com/cloudneutral/cockroachdb-test/issues). 
* Please provide as much information as possible with the issue report, we like to know the version of Spring Data 
that you are using and JVM version, complete stack traces and any relevant configuration information.
* If you need to paste code, or include a stack trace format it as code using triple backtick.

## Versioning

This library follows [Semantic Versioning](http://semver.org/).

## Building from Source

CockroachDB Test requires Java 17 (or later) LTS. 

### Prerequisites

- JDK17+ LTS for building (OpenJDK compatible)
- Maven 3+ (optional, embedded)

If you want to build with the regular `mvn` command,
you will need [Maven v3.x](https://maven.apache.org/run-maven/index.html) or above.

Install the JDK (Linux):

```bash
sudo apt-get -qq install -y openjdk-17-jdk
```

Install the JDK (macOS):

```bash
brew install openjdk@17 
```

### Clone the project

```bash
git clone git@github.com:cloudneutral/cockroachdb-test.git
cd cockroachdb-test
```

### Build the project

```bash
chmod +x mvnw
./mvnw clean install
```

If you want to build with the regular mvn command, you will need [Maven v3.5.0](https://maven.apache.org/run-maven/index.html) or above.

## Terms of Use

See [MIT](LICENSE.txt) for terms and conditions.
