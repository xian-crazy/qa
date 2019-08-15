package com.quvideo.qa.common.tools;

public class JsonFormatUtil {

    /**
     * 对json字符串格式化输出
     * @param jsonStr  json字符串
     * @return  pre格式化的json字符串
     */
    public static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr)) return "";
        StringBuilder sb = new StringBuilder();
        char last = '\0';
        char current = '\0';
        int indent = 0;
        sb.append("<pre >");
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '{':
                case '[':
                    sb.append("<span style=\"color:#00AA00;font-weight:bold\">");
                    sb.append(current);
                    sb.append("</span>");
                    sb.append('\n');
                    indent++;
                    addIndentBlank(sb, indent);
                    break;
                case '}':
                case ']':
                    sb.append("<span style=\"color:#00AA00;font-weight:bold\">");
                    sb.append('\n');
                    sb.append("</span>");
                    indent--;
                    addIndentBlank(sb, indent);
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\') {
                        sb.append('\n');
                        addIndentBlank(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }
        sb.append("</pre >");
        return sb.toString();
    }

    /**
     * 添加space
     * @param sb  字符串
     * @param indent space /t 数量
     */
    private static void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append('\t');
        }
    }
}
