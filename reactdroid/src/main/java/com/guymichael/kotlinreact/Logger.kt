package com.guymichael.kotlinreact

import com.guymichael.promise.LoggerIntf
import kotlin.reflect.KClass

object Logger {
    var mLogger: LoggerIntf? = null

    fun init(logger: LoggerIntf) {
        mLogger = logger
    }

    @JvmStatic
    fun i(cls: Class<*>, msg: String) {
        mLogger?.takeIf { it.shouldLogI() }?.i(cls.simpleName, msg)
    }

    fun i(cls: KClass<*>, msg: String) {
        mLogger?.takeIf { it.shouldLogI() }?.i(cls.java.simpleName, msg)
    }

    @JvmStatic
    fun d(cls: Class<*>, msg: String) {
        mLogger?.takeIf { it.shouldLogD() }?.d(cls.simpleName, msg)
    }

    fun d(cls: KClass<*>, msg: String) {
        mLogger?.takeIf { it.shouldLogD() }?.d(cls.java.simpleName, msg)
    }

    @JvmStatic
    fun w(cls: Class<*>, msg: String) {
        mLogger?.takeIf { it.shouldLogW() }?.w(cls.simpleName, msg)
    }

    fun w(cls: KClass<*>, msg: String) {
        mLogger?.takeIf { it.shouldLogW() }?.w(cls.java.simpleName, msg)
    }

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




    /** See [LoggerIntf.shouldLogI] */
    fun iLazy(cls: Class<*>, messageSupplier: () -> String) {
        mLogger?.takeIf { it.shouldLogI() }?.i(cls.simpleName, messageSupplier())
    }

    /** See [LoggerIntf.shouldLogD] */
    fun dLazy(cls: Class<*>, messageSupplier: () -> String) {
        mLogger?.takeIf { it.shouldLogD() }?.d(cls.simpleName, messageSupplier())
    }

    /** See [LoggerIntf.shouldLogW] */
    fun wLazy(cls: Class<*>, messageSupplier: () -> String) {
        mLogger?.takeIf { it.shouldLogW() }?.i(cls.simpleName, messageSupplier())
    }

    /** See [LoggerIntf.shouldLogE] */
    fun eLazy(cls: Class<*>, messageSupplier: () -> String) {
        mLogger?.takeIf { it.shouldLogE() }?.e(cls.simpleName, messageSupplier())
    }
}