import java.io.IOException;

import org.infinispan.Cache;
import org.infinispan.CacheImpl;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class InfinispanPrintConfiguration {
    public static void main(String[] args) throws IOException {
        EmbeddedCacheManager manager =
            new DefaultCacheManager("infinispan.xml");
        Cache<?, ?> cache = manager.getCache();

        System.out.println(cache
                           .getCacheConfiguration()
                           .toString());

        System.out.println(((CacheImpl) cache).getConfigurationAsXmlString());

        System.out.println(((CacheImpl) manager.getCache("defaultNamedCache")).getConfigurationAsXmlString());

        manager.stop();
    }
}