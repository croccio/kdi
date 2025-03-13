package it.croccio.kdi.injectable

class BindSingleton<in T, out U : @UnsafeVariance T>(
    factory: () -> U,
) : Instance<T, U> {

    private val entity: U by lazy(factory)

    override fun get(): U = entity

}