// This example demonstrates how to write and test Mill plugin, and publish it to
// Sonatype's Maven Central so it can be used by other developers over the internet
// via xref:Import_File_And_Import_Ivy.adoc[import $ivy].

// == Project Configuration

import mill._, scalalib._, publish._
import mill.main.BuildInfo.millVersion

object myplugin extends ScalaModule with PublishModule {
  def scalaVersion = "2.13.8"

  def ivyDeps = Agg(ivy"com.lihaoyi:mill-dist:$millVersion")

  // Testing Config
  object test extends ScalaTests with TestModule.Utest{
    def ivyDeps = Agg(ivy"com.lihaoyi::mill-testkit:$millVersion")
    def forkEnv = Map("MILL_EXECUTABLE_PATH" -> millExecutable.assembly().path.toString)

    object millExecutable extends JavaModule{
      def ivyDeps = Agg(ivy"com.lihaoyi:mill-dist:$millVersion")
      def mainClass = Some("mill.runner.client.MillClientMain")
      def resources = T{
        val p = T.dest / "mill" / "local-test-overrides" / s"com.lihaoyi-${myplugin.artifactId()}"
        os.write(p, myplugin.localClasspath().map(_.path).mkString("\n"), createFolders = true)
        Seq(PathRef(T.dest))
      }
    }
  }

  // Publishing Config
  def publishVersion = "0.0.1"

  def pomSettings = PomSettings(
    description = "Line Count Mill Plugin",
    organization = "com.lihaoyi",
    url = "https://github.com/lihaoyi/myplugin",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("lihaoyi", "myplugin"),
    developers = Seq(Developer("lihaoyi", "Li Haoyi", "https://github.com/lihaoyi"))
  )
}

// Mill plugins are fundamentally just JVM libraries that depend on Mill. Any vanilla JVM
// library (whether written in Java or Scala) can be used in a Mill `build.sc` file via
// `import $ivy`, but Mill plugins tend to integrate with Mill by defining a
// xref:Modules.adoc#_trait_modules[trait for modules in your `build.sc` to inherit from].
//
// The above `build.sc` file sets up a `object myplugin extends ScalaModule` not just to
// compile your Mill plugin project, but also to run automated tests using `mill-testkit`,
// and to configure it for publishing to Maven Central via `PublishModule`
//
// Like any other `trait`, a Mill plugin's traits modules allow you to:
//
// * Add additional tasks to an existing module
// * Override existing tasks, possibly referencing the old task via `super`
// * Define abstract tasks that the final module must implement
//
// In this example, we define a `LineCountJavaModule` that does all of the above:
// it defines an abstract `def lineCountResourceFileName` task, it adds an additional
// `def lineCount` task, and it overrides the `def resources`:

// == Plugin Implementation

/** See Also: myplugin/src/LineCountJavaModule.scala */

// This is a synthetic example, but it serves to illustrate how Mill plugins are typically
// defined. The plugin can be compiled via:

/** Usage

> ./mill myplugin.compile
compiling 1 Scala source...

*/


// Mill provides the `mill-testkit` library to make it easy for you to test your Mill
// plugin. The example project above has set up tests that can be run via the normal `.test`
// command, as shown below:

/** Usage

> ./mill myplugin.test
+ myplugin.UnitTests.unit...
+ myplugin.IntegrationTests.integration...
+ myplugin.ExampleTests.example...
...
*/

// `mill-testkit` is the same set of helpers that Mill uses internally for its
// own testing, and covers three approaches:
//
// == Unit Tests
//
// These are tests that run in-process, with the Mill `build.sc` defined as a `TestBaseModule`,
// and using a `UnitTester` to run its tasks and inspect their output. `UnitTester` is provided
// a path to a folder on disk containing the files that are to be built with the given `TestBaseModule`,
// and can be used to evaluate tasks (by direct reference or by string-selector) and inspect
// the results in-memory:

/** See Also: myplugin/test/src/mill/testkit/UnitTests.scala */
/** See Also: myplugin/test/resources/unit-test-project/src/foo/Foo.java */

// Mill Unit tests are good for exercising most kinds of business logic in Mill plugins. Their
// main limitation is that they do not exercise the Mill subprocess-launch and bootstrap process,
// but that should not be a concern for most Mill plugins.
//
// == Integration Tests
//
// Integration tests are one step up from Unit tests: they are significantly slower to run due
// to running Mill in a subprocess, but are able to exercise the end-to-end lifecycle of a Mill
// command. Unlike unit tests which define a `TestRootModule` in-memory as part of the test code,
// Mill's integration tests rely on a `build.sc` that is processed and compiled as part of the
// test initialization, and can only perform assertions on the four things that are returned from
// any subprocess:
//
// 1. `.isSuccess: Boolean`, whether or the Mill subprocess returned with exit code 0
// 2. `.out: String`, the standard output captured by the Mill process
// 3. `.err: String`, the standard error captured by the Mill process
// 4. Any files that are generated on disk. In particular, files generated by tasks
//    can be fetched via the `tester.out("...").*` APIs to be read as JSON strings (via `.text`),
//    parsed `ujson.Value` ASTs (`.json`), or parsed into a typed Scala value (`.value[T]`)

/** See Also: myplugin/test/src/mill/testkit/IntegrationTests.scala */
/** See Also: myplugin/test/resources/integration-test-project/src/foo/Foo.java */
/** See Also: myplugin/test/resources/integration-test-project/build.sc */

// Integration tests are generally used sparingly, but they are handy for scenarios where
// your Mill plugin logic prints to standard output or standard error, and you want to assert
// that the printed output is as expected.
//
// == Example Tests
//
// Example tests are a variant of the integration tests mentioned above, but instead of
// having the testing logic defined as part of the test suite in a `.scala` file, the testing
// logic is instead defined as a `/** Usage */` comment in the `build.sc`. These tests are
// a great way of documenting expected usage of your plugin: a user can glance at a single
// file to see how the plugin is imported (via `import $ivy`) how it is used (e.g. by being
// extended by a module) and what commands they can run exercising the plugin and the resultant
// output they should expect:

/** See Also: myplugin/test/src/mill/testkit/ExampleTests.scala */
/** See Also: myplugin/test/resources/example-test-project/src/foo/Foo.java */
/** See Also: myplugin/test/resources/example-test-project/build.sc */

// The `/** Usage */` comment is of the following format:
//
// * Each line prefixed with `>` is a command that is to be run
// * Following lines after commands are expected output, until the next blank line
// * If the command is expected to fail, the following lines should be prefixed by `error: `
// * Expected output lines can contain `...` wildcards to match against parts of the output
//   which are unstable or unnecessary for someone reading through the `build.sc`.
// * A `...` wildcard on its own line can be used to match against any number of additional
//   lines of output
//
// The line-matching for example tests is intentionally fuzzy: it does not assert that the
// ordering of the lines printed by the command matches the ordering given, as long as every
// line printed is given and every line given is printed. `...` wildcards intentionally add
// additional fuzziness to the matching. The point of example tests is not to match
// character-for-character exactly what the output must be, but to match on the "important"
// parts of the output while simultaneously emphasizing these important parts to someone who
// may be reading the `build.sc`
//
// Example tests are similar to integration tests in that they exercise the full Mill bootstrapping
// process, and are thus much slower and more expensive to run than unit tests. However, it is
// usually a good idea to have at least one example test for your Mill plugin, so a user who
// wishes to use it can take the `build.sc` and associated files and immediately have a Mill
// build that is runnable using your plugin, along with a list of commands they can run and
// what output they should expect.
//
// == Publishing

/** Usage

> sed -i 's/0.0.1/0.0.2/g' build.sc

> ./mill myplugin.publishLocal
Publishing Artifact(com.lihaoyi,myplugin_2.13,0.0.2) to ivy repo...

*/
// Mill plugins are JVM libraries like any other library written in Java or Scala. Thus they
// are published the same way: by extending `PublishModule` and defining the module's `publishVersion`
// and `pomSettings`. Once done, you can publish the plugin locally via `publishLocal` below,
// or to Maven Central via `mill.scalalib.public.PublishModule/publishAll`for other developers to
// use.

