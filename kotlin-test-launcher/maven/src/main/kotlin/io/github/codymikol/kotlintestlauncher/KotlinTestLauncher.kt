import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.LifecyclePhase

@Mojo(name = "test", defaultPhase = LifecyclePhase.VERIFY)
class KotlinTestLauncherPlugin : AbstractMojo() {

    @Throws(MojoExecutionException::class)
    override fun execute() {
        log.info("Dicover and run tests here...")
    }

}

