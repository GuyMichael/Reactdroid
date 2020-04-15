package com.guymichael.reactdroid

import com.guymichael.promise.LoggerIntf
import kotlin.reflect.KClass

object Logger {
    var mLogger: LoggerIntf? = null

    fun init(logger: LoggerIntf) {
        mLogger = logger
    }

    fun i(cls: Class<*>, msg: String) {
        mLogger?.takeIf { it.shouldLogI() }?.i(cls.simpleName, msg)
    }

    fun d(cls: Class<*>, msg: String) {
        mLogger?.takeIf { it.shouldLogD() }?.d(cls.simpleName, msg)
    }

    @Deprecated("use the KClass method")
    @JvmStatic
    fun w(cls: Class<*>, msg: String) {
        mLogger?.takeIf { it.shouldLogW() }?.w(cls.simpleName, msg)
    }

    fun w(cls: KClass<*>, msg: String) {
        mLogger?.takeIf { it.shouldLogW() }?.w(cls.java.simpleName, msg)
    }

    @Deprecated("use the KClass method")
    @JvmStatic
    fun e(cls: Class<*>, msg: String) {
        mLogger?.takeIf { it.shouldLogE() }?.e(cls.simpleName, msg)
    }

    fun e(cls: KClass<*>, msg: String) {
        mLogger?.takeIf { it.shouldLogE() }?.e(cls.java.simpleName, msg)
    }

    fun i(tag: String, msg: String) {
        mLogger?.takeIf { it.shouldLogI() }?.i(tag, msg)
    }

    fun d(tag: String, msg: String) {
        mLogger?.takeIf { it.shouldLogD() }?.d(tag, msg)
    }

    fun w(tag: String, msg: String) {
        mLogger?.takeIf { it.shouldLogW() }?.w(tag, msg)
    }

    fun e(tag: String, msg: String) {
        mLogger?.takeIf { it.shouldLogE() }?.e(tag, msg)
    }





    fun iLazy(cls: Class<*>, messageSupplier: () -> String) {
        mLogger?.takeIf { it.shouldLogI() }?.i(cls.simpleName, messageSupplier())
    }

    fun dLazy(cls: Class<*>, messageSupplier: () -> String) {
        mLogger?.takeIf { it.shouldLogD() }?.d(cls.simpleName, messageSupplier())
    }

    fun wLazy(cls: Class<*>, messageSupplier: () -> String) {
        mLogger?.takeIf { it.shouldLogW() }?.i(cls.simpleName, messageSupplier())
    }

    fun eLazy(cls: Class<*>, messageSupplier: () -> String) {
        mLogger?.takeIf { it.shouldLogE() }?.e(cls.simpleName, messageSupplier())
    }
}