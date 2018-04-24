package com.ecit.common.utils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * 占位符替换工具
 */
public class MustacheUtils {
    private static volatile MustacheFactory single = null;

    private MustacheUtils() {}

    public static MustacheFactory getInstance() {
        if (single == null) {
            synchronized(MustacheUtils.class){
                // 注意：里面的判断是一定要加的，否则出现线程安全问题
                if (single == null) {
                    single = new DefaultMustacheFactory();
                }
            }
        }
        return single;
    }

    /**
     * 字符串替换占位符
     * @param source
     * @param params
     * @return
     */
    public static String mustacheString(String source, Map<String, Object> params) {
        Mustache mustache = MustacheUtils.getInstance().compile(new StringReader(source), null);
        Writer writer = new StringWriter();
        mustache.execute(writer, params);
        try {
            writer.close();
        } catch (IOException e) {
            return null;
        }
        return writer.toString();
    }

    /**
     * 文件替换占位符
     * @param source
     * @param params
     * @return
     */
    public static String mustacheFile(String source, Map<String, Object> params) {
        Mustache mustache = MustacheUtils.getInstance().compile(source);
        Writer writer = new StringWriter();
        mustache.execute(writer, params);
        try {
            writer.close();
        } catch (IOException e) {
            return null;
        }
        return writer.toString();
    }

}
