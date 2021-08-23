package me.kesx.tool.util;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DateUtil {
    public Date readableDateFormat(Date date) {
        Date readableDate = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            readableDate =  simpleDateFormat.parse(simpleDateFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return readableDate;
    }
    public String dateFormat(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        return simpleDateFormat.format(date);
    }
    public boolean compareStringDateSmaller(String s1,String s2){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        try {
            return (simpleDateFormat.parse(s1).before(simpleDateFormat.parse(s2)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

}
