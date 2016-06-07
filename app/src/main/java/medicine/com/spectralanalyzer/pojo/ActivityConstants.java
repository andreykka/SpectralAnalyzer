package medicine.com.spectralanalyzer.pojo;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ActivityConstants {

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormat.forPattern("MM'm'dd'd'_hh_mm_sss");

    public static final String PATH_NAME = "pathName";

    public static final String FILE_TO_PROCESS = "fileToProcess";

    public static final String PROCESS_RESULT_PARAM = "processResultParam";

}
