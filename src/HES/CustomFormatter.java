package HES;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Marina
 * Classe servant a formater le logger
 */

public class CustomFormatter extends Formatter {


    @Override
    public String format(LogRecord record) {

        StringBuilder sb = new StringBuilder();

        Date date = new Date(record.getMillis());
        sb.append(date.getTime());
        sb.append(" ; ");

        sb.append(record.getLevel().getName());
        sb.append(" ; ");

        sb.append(record.getSourceClassName());
        sb.append(" ; ");

        sb.append(formatMessage(record));
        sb.append("\r\n");

        return sb.toString();
    }
}
