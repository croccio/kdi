package it.croccio.kdi

import it.croccio.kdi.exception.KDIException
import it.croccio.kdi.injectable.Instance


@PublishedApi
internal val injections = mutableMapOf<String, Instance<*, *>>()

@PublishedApi
internal inline fun <reified T> findInjection(): T =
    injections[T::class.toString()]
        ?.run { get() as T }
        ?: throw KDIException(T::class)

inline fun <reified T> inject(vararg instances: Instance<T, *>) where T : Any {
    instances
        .forEach { instance -> injections[T::class.toString()] = instance }
}

fun <T> inject(vararg modules: T) where T : Module {
    modules
        .forEach { module -> module.register() }
}

inline fun <reified T> injection(): InjectDelegation<T> =
    InjectDelegation<T>(findInjection())

inline fun <reified T> byInjection(): T {
    val toInject by injection<T>()
    return toInject
}