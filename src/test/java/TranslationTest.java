import javax.inject.Inject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.daisy.maven.xproc.xprocspec.XProcSpecRunner;

import org.daisy.pipeline.braille.common.CSSStyledTextTransform;
import org.daisy.pipeline.braille.common.TextTransform;
import org.daisy.pipeline.braille.common.Transform;
import static org.daisy.pipeline.braille.common.Transform.Provider.util.dispatch;
import static org.daisy.pipeline.pax.exam.Options.brailleModule;
import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.forThisPlatform;
import static org.daisy.pipeline.pax.exam.Options.logbackBundles;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;
import static org.daisy.pipeline.pax.exam.Options.xprocspecBundles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TranslationTest {
	
	@Inject
	private BundleContext context;
	
	@Test
	public void testEmail() throws Exception {
		List<Transform.Provider<TextTransform>> providers = new ArrayList<Transform.Provider<TextTransform>>();
		for (ServiceReference<? extends TextTransform.Provider> ref : context.getServiceReferences(TextTransform.Provider.class, null))
			providers.add(context.getService(ref));
		TextTransform translator = dispatch(providers).get("(translator:nlb)(grade:1)").iterator().next();
		assertEquals(
			"⠋⠕⠕ ⠣⠋⠕⠕⠃⠁⠗⠈⠝⠇⠃⠄⠝⠕⠜ ⠃⠼",
			translator.transform("foo foobar@nlb.no bar"));
	}
	
	@Test
	public void testTextTransformUncontracted() throws Exception {
		List<Transform.Provider<CSSStyledTextTransform>> providers = new ArrayList<Transform.Provider<CSSStyledTextTransform>>();
		for (ServiceReference<? extends CSSStyledTextTransform.Provider> ref : context.getServiceReferences(CSSStyledTextTransform.Provider.class, null))
			providers.add(context.getService(ref));
		CSSStyledTextTransform translator = dispatch(providers).get("(translator:nlb)(grade:1)").iterator().next();
		assertEquals(
			new String[]{"⠋⠕⠕⠃⠼ ","⠋⠕⠕⠃⠁⠗"},
			translator.transform(new String[]{"foobar ","foobar"}, new String[]{"","text-transform: uncontracted"}));
	}
	
	@Inject
	private XProcSpecRunner xprocspecRunner;
	
	@Test
	public void runXProcSpec() throws Exception {
		File baseDir = new File(PathUtils.getBaseDir());
		boolean success = xprocspecRunner.run(new File(baseDir, "src/test/xprocspec"),
		                                      new File(baseDir, "target/xprocspec-reports"),
		                                      new File(baseDir, "target/surefire-reports"),
		                                      new File(baseDir, "target/xprocspec"),
		                                      new XProcSpecRunner.Reporter.DefaultReporter());
		assertTrue("XProcSpec tests should run with success", success);
	}
	
	@Configuration
	public Option[] config() {
		return options(
			logbackConfigFile(),
			logbackBundles(),
			domTraversalPackage(),
			felixDeclarativeServices(),
			mavenBundle().groupId("com.google.guava").artifactId("guava").versionAsInProject(),
			mavenBundle().groupId("net.java.dev.jna").artifactId("jna").versionAsInProject(),
			mavenBundle().groupId("org.daisy.bindings").artifactId("jhyphen").versionAsInProject(),
			mavenBundle().groupId("org.liblouis").artifactId("liblouis-java").versionAsInProject(),
			mavenBundle().groupId("org.daisy.braille").artifactId("braille-utils.api").versionAsInProject(),
			mavenBundle().groupId("org.daisy.libs").artifactId("jstyleparser").versionAsInProject(),
			mavenBundle().groupId("org.unbescape").artifactId("unbescape").versionAsInProject(),
			mavenBundle().groupId("org.daisy.braille").artifactId("braille-css").versionAsInProject(),
			mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.antlr-runtime").versionAsInProject(),
			brailleModule("common-utils"),
			brailleModule("css-core"),
			brailleModule("css-calabash"),
			brailleModule("css-utils"),
			brailleModule("pef-core"),
			brailleModule("liblouis-core"),
			brailleModule("liblouis-saxon"),
			brailleModule("liblouis-calabash"),
			brailleModule("liblouis-utils"),
			brailleModule("liblouis-tables"),
			forThisPlatform(brailleModule("liblouis-native")),
			brailleModule("libhyphen-core"),
			brailleModule("libhyphen-libreoffice-tables"),
			forThisPlatform(brailleModule("libhyphen-native")),
			thisBundle(),
			xprocspecBundles(),
			junitBundles()
		);
	}
	
}
