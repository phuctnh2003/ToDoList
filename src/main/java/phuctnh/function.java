package phuctnh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class function {
    //<-----------MÃ MODULE------------>
    //01: Database
    //02: Mapping
    //<-----------MÃ LỖI------------>
    // 400: Không tìm thấy dữ liệu
    // 500: Xử lý dữ liệu thất bại
    // 505: Ngoại lệ SQL
    // 200: Thành công
    private static final Logger logger = LoggerFactory.getLogger(function.class);
    public void log(String message) {
        logger.error("LOG ERROR: {}", message);
    }
    public void log_info(String message)
    {
        logger.info("LOG INFO: {}", message);
    }

    public void logExceptionWithCode(String code, Exception e) {
        logger.error("Code {}: Exception occurred", code, e);
    }
}
