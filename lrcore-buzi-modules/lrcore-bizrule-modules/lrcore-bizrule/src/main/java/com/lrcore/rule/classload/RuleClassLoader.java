package com.lrcore.rule.classload;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * <>类模块说明</p>
 *
 * @Describe:
 * @ClassName: RuleClassLoader
 * @Author: Qi Liu
 * @Date: 2026/8/3 17:49
 * @Version: 1.0
 */
public class RuleClassLoader extends URLClassLoader {
    public RuleClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public static RuleClassLoader create(Path jarPath, ClassLoader parent) throws MalformedURLException {
        return new RuleClassLoader(new URL[]{jarPath.toUri().toURL()}, parent);
    }
}
