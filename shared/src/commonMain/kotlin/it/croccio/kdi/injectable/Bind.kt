package it.croccio.kdi.injectable

class Bind<in T, out U : @UnsafeVariance T>(
    private val factory: () -> U,
) : Instance<T, U> {

    override fun get(): U = factory()

}