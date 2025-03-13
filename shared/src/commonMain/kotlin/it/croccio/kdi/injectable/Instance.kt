package it.croccio.kdi.injectable

sealed interface Instance<in T, out U> {

    fun get(): U

}
