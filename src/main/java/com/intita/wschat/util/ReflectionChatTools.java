package com.intita.wschat.util;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by roma on 14.06.17.
 */
public class ReflectionChatTools {

    private static String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class[] getClasses(String packageName, Class requiredAnnotation)
            throws ClassNotFoundException, IOException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        List<Class> candidates = new ArrayList<Class>();
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(packageName) + "/" + "**/*.class";
        Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                if (isCandidate(metadataReader,requiredAnnotation)) {
                    candidates.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
                }
            }
        }
        return candidates.toArray(new Class[candidates.size()]);
    }

    private static boolean isCandidate(MetadataReader metadataReader,Class requiredAnnotation) throws ClassNotFoundException
    {
        try {
            Class c = Class.forName(metadataReader.getClassMetadata().getClassName());
            if (c.getAnnotation(requiredAnnotation) != null) {
                return true;
            }
        }
        catch(Throwable e){
        }
        return false;
    }

    public static List<Method> getMethods( Class targetClass){
        List<Method> methods =  Arrays.asList(targetClass.getDeclaredMethods());
        return methods;
    }
    public static List<Method> getMethodsFromClasses(List<Class> classes){
    List<Method> methods = new ArrayList<Method>();
    for (Class currentClass : classes) {
        methods.addAll(getMethods(currentClass));
    }
    return methods;

    }

    public static List<Method> getMethodsFromPackage(String packageName,Class requiredClassAnotation){
        List<Class> classes = null;
        try {
            classes = Arrays.asList(getClasses(packageName,requiredClassAnotation));
        }
        catch(Exception e){
            return new ArrayList<Method>();
        }
        List<Method> methods = getMethodsFromClasses(classes);
        return methods;


    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
