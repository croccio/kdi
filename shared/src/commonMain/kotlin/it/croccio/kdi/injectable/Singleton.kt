package it.croccio.kdi.injectable

class Singleton<T>(
    factory: () -> T,
) : Instance<T, T> {

    private val entity: T by lazy { factory() }

    override fun get(): T = entity

}