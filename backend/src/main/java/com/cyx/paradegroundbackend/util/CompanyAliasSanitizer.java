package com.cyx.paradegroundbackend.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CompanyAliasSanitizer {

    private static final Map<Pattern, String> ALIAS_RULES = new LinkedHashMap<>();

    static {
        ALIAS_RULES.put(Pattern.compile("阿里巴巴|阿里云|阿里"), "阿巴阿巴");
        ALIAS_RULES.put(Pattern.compile("\\bAlibaba\\b", Pattern.CASE_INSENSITIVE), "阿巴阿巴");
        ALIAS_RULES.put(Pattern.compile("腾讯云|腾讯"), "企鹅大王");
        ALIAS_RULES.put(Pattern.compile("\\bTencent\\b", Pattern.CASE_INSENSITIVE), "企鹅大王");
        ALIAS_RULES.put(Pattern.compile("字节跳动|字节"), "字符跳跃");
        ALIAS_RULES.put(Pattern.compile("\\bByteDance\\b", Pattern.CASE_INSENSITIVE), "字符跳跃");
        ALIAS_RULES.put(Pattern.compile("美团"), "美味军团");
        ALIAS_RULES.put(Pattern.compile("\\bMeituan\\b", Pattern.CASE_INSENSITIVE), "美味军团");
        ALIAS_RULES.put(Pattern.compile("京东"), "晶咚");
        ALIAS_RULES.put(Pattern.compile("\\bJD(?:\\.com)?\\b", Pattern.CASE_INSENSITIVE), "晶咚");
        ALIAS_RULES.put(Pattern.compile("拼多多"), "拼很多");
        ALIAS_RULES.put(Pattern.compile("\\bPinduoduo\\b", Pattern.CASE_INSENSITIVE), "拼很多");
        ALIAS_RULES.put(Pattern.compile("小米"), "小颗粒科技");
        ALIAS_RULES.put(Pattern.compile("\\bXiaomi\\b", Pattern.CASE_INSENSITIVE), "小颗粒科技");
        ALIAS_RULES.put(Pattern.compile("百度"), "摆渡一下");
        ALIAS_RULES.put(Pattern.compile("\\bBaidu\\b", Pattern.CASE_INSENSITIVE), "摆渡一下");
        ALIAS_RULES.put(Pattern.compile("华为"), "花火科技");
        ALIAS_RULES.put(Pattern.compile("\\bHuawei\\b", Pattern.CASE_INSENSITIVE), "花火科技");
        ALIAS_RULES.put(Pattern.compile("滴滴"), "嘀一下出行");
        ALIAS_RULES.put(Pattern.compile("\\bDidi\\b", Pattern.CASE_INSENSITIVE), "嘀一下出行");
        ALIAS_RULES.put(Pattern.compile("网易"), "网一网");
        ALIAS_RULES.put(Pattern.compile("\\bNetEase\\b", Pattern.CASE_INSENSITIVE), "网一网");
        ALIAS_RULES.put(Pattern.compile("快手"), "快一点视频");
        ALIAS_RULES.put(Pattern.compile("\\bKuaishou\\b", Pattern.CASE_INSENSITIVE), "快一点视频");
    }

    public String sanitizeText(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String sanitized = text;
        for (Map.Entry<Pattern, String> entry : ALIAS_RULES.entrySet()) {
            sanitized = entry.getKey().matcher(sanitized).replaceAll(entry.getValue());
        }
        return sanitized;
    }
}
