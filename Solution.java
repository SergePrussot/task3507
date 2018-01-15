package com.javarush.task.task35.task3507;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* 
ClassLoader - что это такое?
*/
public class Solution {
    public static void main(String[] args) throws Exception {
        Set<? extends Animal> allAnimals = getAllAnimals(Solution.class.getProtectionDomain().getCodeSource().getLocation().getPath() +
                Solution.class.getPackage().getName().replaceAll("[.]", "/") + "/data");
        System.out.println(allAnimals);
    }

    public static Set<? extends Animal> getAllAnimals(String pathToAnimals) throws Exception {
        File[] files = new File(pathToAnimals).listFiles();
        Set<Animal> result = new HashSet<>();
        for (File file: files) {
            Class cls = loadClass(file);
            Animal object = createObject(cls);
            if (object != null) {
                result.add(object);
            }
        }
        return result;
    }

    private static Animal createObject(Class cls) throws Exception {
        if (cls == null){
            return null;
        }
        for (Class interf: cls.getInterfaces()) {
            if (interf.isAssignableFrom(Animal.class)) {
                for (Constructor constr: cls.getConstructors()) {
                    if (Modifier.isPublic(constr.getModifiers())) {
                        return (Animal)cls.newInstance();
                    }
                }
            }
        }
        return null;
    }

    private static Class loadClass(File file) throws ClassNotFoundException {
        ClassLoader classLoader = new MyClassLoader(Solution.class.getClassLoader());
        Class aClass = classLoader.loadClass(file.getAbsolutePath());
        return aClass;
    }

    private static class MyClassLoader extends ClassLoader{

        public MyClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class loadClass(String name) throws ClassNotFoundException {
            if (!name.endsWith(".class")) {
                return null;

            }
            try {
                String url = "file:" + name;
                URL myUrl = new URL(url);
                URLConnection connection = myUrl.openConnection();
                InputStream input = connection.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int data = input.read();

                while(data != -1){
                    buffer.write(data);
                    data = input.read();
                }

                input.close();

                byte[] classData = buffer.toByteArray();

                return defineClass(null,
                        classData, 0, classData.length);

            } catch (Exception e) {
                return super.findClass(name);
            }
        }

    }
}

