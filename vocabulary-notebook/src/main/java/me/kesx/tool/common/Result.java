package me.kesx.tool.common;

public class Result<T> {
    private final Integer code;
    private final String message;
    private final T data;

    public Result(T data) {
        this.code = ResultEnum.SUCCESS.getCode();
        this.message = ResultEnum.SUCCESS.getMsg();
        this.data = data;
    }
    public Result(String message) {
        this.code = ResultEnum.ERROR.getCode();
        this.message = message;
        this.data = null;
    }
    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.message = msg;
        this.data = data;
    }

    public static <T> Result<T> build (Integer code,String message,T data){return new Result<>(code,message,data);}

    public static <String> Result<String> success(){
        return Result.build(ResultEnum.SUCCESS.getCode(),ResultEnum.SUCCESS.getMsg(),null);
    }

    public static <T> Result<T> success(T data) {return  new Result<>(data);}

    public static <T> Result<T> error(String msg) {return new Result<>(msg);}

    public static <T> Result<T> error(ResultEnum resultEnum,T data) {
        return Result.build(resultEnum.getCode(),resultEnum.getMsg(),data);
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
