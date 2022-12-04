package com.zhaoyanpeng.subzlib.exception;

/**
 * NoDeleteJuageFoundException
 *
 * @author zhaoyanpeng
 * @date 2022/12/4 17:12
 */
public class InitDeleteJuageError extends RuntimeException{
    public InitDeleteJuageError(String message) {
        super(message);
    }
}
