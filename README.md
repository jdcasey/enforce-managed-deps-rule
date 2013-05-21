# Managed-Dependencies Requirement Rule for `maven-enforcer-plugin`

This is a rule implementation for the Apache Maven Enforcer Plugin, version 1.0+. It simply checks that all dependencies in your POM have versions that were specified in a `<dependencyManagement>` section.
  
## Why??

When you have a large project, it's usually good practice to accumulate all the dependency declarations in the top-level POM, in a `<dependencyManagement>` section. This gives you a single place to look to find the version, typical scope, and exclusions for any dependency used anywhere in your project. Of course, you may need to change the scope, or less often, the exclusions in different sub-POMs...but you probably **don't** want to change the version in a sub-POM, at least in 99.999% of cases.
  
This rule helps you enforce that best practice. While it doesn't check that only one POM in the build tree contains a `<dependencyManagement>` element, it will verify that all versions for dependencies are managed somewhere.
  
## How to Use

In your `pom.xml`, simply specify an enforcer-plugin configuration similar to this:

    <build>
      <plugins>
        <plugin>
          <artifactId>maven-enforcer-plugin</artifactId>
          <version>1.0</version>
          <dependencies>
            <dependency>
              <groupId>org.commonjava.maven.enforcer</groupId>
              <artifactId>enforce-managed-deps-rule</artifactId>
              <version>1.0</version>
            </dependency>
          </dependencies>
          <executions>
            <execution>
              <id>no-managed-deps</id>
              <goals>
                <goal>enforce</goal>
              </goals>
              <phase>initialize</phase>
              <configuration>
                <rules>
                  <requireManagedDeps implementation="org.commonjava.maven.enforcer.rule.EnforceManagedDepsRule">
                    <checkProfiles>true</checkProfiles>
                    <failOnViolation>true</failOnViolation>
                  </requireManagedDeps>
                </rules>
              </configuration>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>

## Options

You may notice the two configuration parameters for the rule: `checkProfiles` and `failOnViolation`. These have the following meanings:

### `checkProfiles`

If this is enabled, the rule will not only check the dependencies in the main body of the POM, but also dependencies in all profiles. This is usually a good idea, but sometimes the information in POM profiles can become a bit obsolete. Therefore, it's disabled by default.

### `failOnViolation`

If this is disabled, the rule will only print a warning when it comes across a dependency with a locally-specified version. By default, it is enabled...meaning it will actually fail the build if it finds a non-conforming dependency declaration.
