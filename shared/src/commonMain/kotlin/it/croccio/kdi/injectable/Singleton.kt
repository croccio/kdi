package it.croccio.kdi.injectable

class Singleton<T>(
    factory: () -> T,
) : Instance<T, T> {

    private var entity: T = factory()

    override fun get(): T = entity

}