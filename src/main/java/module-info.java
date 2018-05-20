import com.github.danny02.extension.TimeLimitExtension;
import org.junit.jupiter.api.extension.Extension;

module junit.timelimit {
    requires org.junit.jupiter.api;
    requires org.junit.platform.commons;

    exports com.github.danny02.annotation;

    provides Extension with TimeLimitExtension;
}