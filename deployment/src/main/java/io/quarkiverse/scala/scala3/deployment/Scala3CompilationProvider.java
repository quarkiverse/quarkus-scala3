package io.quarkiverse.scala.scala3.deployment;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jboss.logging.Logger;

import dotty.tools.dotc.interfaces.AbstractFile;
import dotty.tools.dotc.interfaces.CompilerCallback;
import dotty.tools.dotc.interfaces.Diagnostic;
import dotty.tools.dotc.interfaces.SimpleReporter;
import dotty.tools.dotc.interfaces.SourceFile;
import io.quarkus.deployment.dev.CompilationProvider;
import io.quarkus.paths.PathCollection;

/**
 * Main.process() documentation for "dotty-interface" overload used here.
 * Architectural Decision Record, see javadoc comment below on why this particular appoach was used
 *
 * Notes:
 * - This requires scala3-compiler in the dependencies and classpath of the consuming application
 * - But it allows Quarkus to remain version-agnostic to Scala 3 compilation
 * - We call the user's Scala 3 library to do the compiling
 *
 */

/** Entry point to the compiler that can be conveniently used with Java reflection.
 *
 *  This entry point can easily be used without depending on the `dotty` package,
 *  you only need to depend on `dotty-interfaces` and call this method using
 *  reflection. This allows you to write code that will work against multiple
 *  versions of dotty without recompilation.
 *
 *  The trade-off is that you can only pass a SimpleReporter to this method
 *  and not a normal Reporter which is more powerful.
 *
 *  Usage example: [[https://github.com/lampepfl/dotty/tree/master/compiler/test/dotty/tools/dotc/InterfaceEntryPointTest.scala]]
 *
 *  @param args       Arguments to pass to the compiler.
 *  @param simple     Used to log errors, warnings, and info messages.
 *                    The default reporter is used if this is `null`.
 *  @param callback   Used to execute custom code during the compilation
 *                    process. No callbacks will be executed if this is `null`.
 *  @return
 */

/**
 * This just here so that tooling doesn't try to attach the above Javadoc to
 * this method
 */
public class Scala3CompilationProvider implements CompilationProvider {

    private final static Logger log = Logger.getLogger(Scala3CompilationProvider.class);

    static String COMPILER_ARGS_ENV_VAR = "QUARKUS_SCALA3_COMPILER_ARGS";
    // Parse the xml file to get the Scala compiler args
    private static final Optional<List<String>> COMPILER_ARGS = getCompilerArgs();

    private static Optional<List<String>> getCompilerArgs() {
        String projectRoot = System.getProperty("user.dir");
        log.info("User dir: " + projectRoot);

        // If the environment variable is set, use that
        if (System.getenv(COMPILER_ARGS_ENV_VAR) != null) {
            log.info("Compiler args from env: " + System.getenv(COMPILER_ARGS_ENV_VAR));
            return getCompilerArgsFromEnv();
        }

        // Check if pom.xml exists
        File pom = new File(projectRoot, "/pom.xml");
        if (!pom.exists()) {
            log.info("No pom.xml found, using compiler args from env");
            return getCompilerArgsFromEnv();
        } else {
            // Scala args is in pom.xml under project -> build -> plugins ->
            // plugin(scala-maven-plugin) -> configuration -> args
            MavenXpp3Reader reader = new MavenXpp3Reader();
            try {
                Model model = reader.read(new FileReader(pom));
                List<String> compilerArgs = new ArrayList<>();
                Plugin plugin = model.getBuild().getPlugins().stream()
                        .filter(p -> p.getArtifactId().equals("scala-maven-plugin")).findFirst().get();

                Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
                for (Xpp3Dom arg : configuration.getChild("args").getChildren()) {
                    if (arg.getValue() == null) {
                        continue;
                    }
                    compilerArgs.add(arg.getValue());
                }

                log.info("Compiler args from pom.xml: " + compilerArgs);
                return Optional.of(compilerArgs);

            } catch (Exception e) {
                log.error(e.getMessage());
                log.error(e.getStackTrace());

                return Optional.empty();
            }
        }
    }

    private static Optional<List<String>> getCompilerArgsFromEnv() {
        String compilerArgsString = System.getenv(COMPILER_ARGS_ENV_VAR);
        if (compilerArgsString == null || compilerArgsString.equals("")) {
            return Optional.empty();
        }
        List<String> compilerArgs = Arrays.asList(compilerArgsString.split(","));
        return Optional.of(compilerArgs);
    }

    @Override
    public Set<String> handledExtensions() {
        return Collections.singleton(".scala");
    }

    @Override
    public void compile(Set<File> files, Context context) {
        List<String> sources = files.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());

        String outdir = context.getOutputDirectory().getAbsolutePath();

        String classpath = context.getClasspath().stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(File.pathSeparator));

        List<String> compilerArgs = new ArrayList<>(sources);
        compilerArgs.add("-d");
        compilerArgs.add(outdir);
        compilerArgs.add("-classpath");
        compilerArgs.add(classpath);
        COMPILER_ARGS.ifPresent(compilerArgs::addAll);

        SimpleReporter reporter = new CustomSimpleReporter();
        CompilerCallback callback = new CustomCompilerCallback();

        try {
            // Reflect to get the Dotty compiler on the application's Classpath
            Class<?> mainClass = Class.forName("dotty.tools.dotc.Main");
            Method process = mainClass.getMethod("process", String[].class, SimpleReporter.class,
                    CompilerCallback.class);
            // Run the compiler by calling dotty.tools.dotc.Main.process
            process.invoke(null, compilerArgs.toArray(String[]::new), reporter, callback);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }
    }

    @Override
    public Path getSourcePath(Path classFilePath, PathCollection sourcePaths, String classesPath) {
        return classFilePath;
    }

    class CustomSimpleReporter implements SimpleReporter {
        Integer errorCount = 0;
        Integer warningCount = 0;

        /**
         * Report a diagnostic.
         *
         * @param diag the diagnostic message to report
         */
        @Override
        public void report(Diagnostic diag) {
            if (diag.level() == Diagnostic.ERROR) {
                errorCount += 1;
                log.error(diag.message());
            }
            if (diag.level() == Diagnostic.WARNING) {
                warningCount += 1;
                log.warn(diag.message());
            }
        }
    }

    // This is a no-op implementation right now, the super() calls invoke void
    // methods
    // But it's useful for future reference I think
    class CustomCompilerCallback implements CompilerCallback {

        /**
         * Called when a class has been generated.
         *
         * @param source         The source file corresponding to this class. Example:
         *                       ./src/library/scala/collection/Seq.scala
         * @param generatedClass The generated classfile for this class. Example:
         *                       ./scala/collection/Seq$.class
         * @param className      The name of this class.
         */
        @Override
        public void onClassGenerated(SourceFile source, AbstractFile generatedClass, String className) {
            CompilerCallback.super.onClassGenerated(source, generatedClass, className);
        }

        /**
         * Called when every class for this file has been generated.
         *
         * @param source The source file. Example:
         *               ./src/library/scala/collection/Seq.scala
         */
        @Override
        public void onSourceCompiled(SourceFile source) {
            CompilerCallback.super.onSourceCompiled(source);
        }
    }
}
