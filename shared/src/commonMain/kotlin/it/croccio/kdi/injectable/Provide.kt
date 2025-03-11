package it.croccio.kdi.injectable

class Provide<T>(
    private val factory: () -> T,
) : Instance<T, T> {

    override fun get(): T = factory()

}