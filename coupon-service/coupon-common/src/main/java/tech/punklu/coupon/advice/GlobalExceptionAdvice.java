package tech.punklu.coupon.advice;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.punklu.coupon.exception.CouponException;
import tech.punklu.coupon.vo.CommonResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常增强处理类
 */
@RestControllerAdvice // 对@Controller和@ResponseBody的增强
public class GlobalExceptionAdvice {

    /**
     * 全局异常统一处理方法
     * @param req
     * @param ex
     * @return
     */
    @ExceptionHandler(value = CouponException.class)  // 异常处理器,捕获CouponException异常并进行处理,声明后，Spring会向此方法注入Request请求及捕获的异常
    public CommonResponse<String> handlerCouponException(HttpServletRequest req, CouponException ex){
        CommonResponse<String> response = new CommonResponse<>(
                -1,
                "business error"
        );
        // 将错误信息写入CommonResponse的data
        response.setData(ex.getMessage());
        return response;
    }


}
