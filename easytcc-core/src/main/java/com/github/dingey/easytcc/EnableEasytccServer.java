package com.github.dingey.easytcc;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.*;
import java.util.Set;

@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({EnableEasytccServer.EasytccServerRegistrar.class})
public @interface EnableEasytccServer {

    class EasytccServerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
        private ResourceLoader resourceLoader;

        @Override
        public void setEnvironment(Environment environment) {
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            new EasytccServerClassPathScanner(registry).doScan("com.github.dingey.easytcc.server");
        }

        class EasytccServerClassPathScanner extends ClassPathBeanDefinitionScanner {

            EasytccServerClassPathScanner(BeanDefinitionRegistry registry) {
                super(registry);
            }

            @Override
            protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
                return super.doScan(basePackages);
            }
        }
    }

}
