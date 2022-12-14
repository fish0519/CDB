package org.util;

public class StringUtil {

    public static StringBuilder addLong(String str)
    {
        int pre = 0;
        long result = 0L;
        StringBuilder sb = new StringBuilder();

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
                    sb.append(result);
                    sb.append("\r\n");
                    result = 0;
                    pre =  i + 1;
                    break;
                }
                case '\n':
                {
                    pre =  i + 1;
                    break;
                }
                default:
                {

                }
            }
        }

        return sb;
    }

    public static void main(String[] args) {
        String str = "1,2,3,4\r\n2,3,4,5\r\n";
        System.out.println(addLong(str));
    }
}
