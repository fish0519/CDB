package org.util;

public class StringUtil {

    public static Long addInt(String str)
    {
        int pre = 0;
        long result = 0L;

        for(int i = 0; i < str.length(); i++)
        {
            switch (str.charAt(i))
            {
                case ',':
                {
                    result += Long.parseLong(str.substring(pre, i));
                    pre =  i + 1;
                    break;
                }
                case '\r':
                {
                    result += Long.parseLong(str.substring(pre, i));
                    return result;
                }
                default:
                {

                }
            }
        }

        return result;
    }

    public static Float addFloat(String str)
    {
        int pre = 0;
        Float result = 0f;

        for(int i = 0; i < str.length(); i++)
        {
            switch (str.charAt(i))
            {
                case ',':
                {
                    result += Float.parseFloat(str.substring(pre, i));
                    pre =  i + 1;
                    break;
                }
                case '\r':
                {
                    result += Float.parseFloat(str.substring(pre, i));
                    return result;
                }
                default:
                {

                }
            }
        }

        return result;
    }

    public static void main(String[] args) {
        String str = "1,2,3,4\r\n";
        long result = addInt(str);
        System.out.println(result);

        String str2 = "1.1,2.2,3.3,4.4\r\n";
        System.out.println(addFloat(str2));
    }
}
