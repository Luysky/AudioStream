package HES;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomFormatter extends Formatter {


    @Override
    public String format(LogRecord record) {

        StringBuffer sb = new StringBuffer();

        Date date = new Date(record.getMillis());
        sb.append(date.toString());
        sb.append("j");

        sb.append(record.getLevel());
        sb.append("j");

        sb.append(record.getSourceClassName());
        sb.append("j");

        sb.append(record.getSourceMethodName());
        sb.append("\r\n");

        return sb.toString();
    }
}
