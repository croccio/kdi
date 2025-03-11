package it.croccio.kdi

import it.croccio.kdi.injectable.Bind
import it.croccio.kdi.injectable.Provide
import it.croccio.kdi.injectable.Singleton
import it.croccio.ticketshare.library.kdi.inject
import it.croccio.ticketshare.library.kdi.injection
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InjectionTests {

    interface IToInject
    data class ToInject(val prop1: String, val prop2: Int) : IToInject

    @Test
    fun `WHEN a SINGLETON is injected THEN the same instance is returned`() {
        inject(Singleton { ToInject("A", 1) })

        val first: ToInject by injection()
        val second: ToInject by injection()
        val third: ToInject by injection()

        assertNotNull(first)
        assertNotNull(second)
        assertNotNull(third)
        assertTrue { first === second && second === third }
    }

    @Test
    fun `WHEN a PROVIDE is injected THEN new instance instance is returned`() {
        inject(Provide { ToInject("A", 1) })

        val first: ToInject by injection()
        val second: ToInject by injection()
        val third: ToInject by injection()

        assertNotNull(first)
        assertNotNull(second)
        assertNotNull(third)
        assertTrue { first !== second && second !== third }
    }

    @Test
    fun `WHEN a BIND is injected THEN new instance with its supertype identifier is returned`() {
        inject(Bind<IToInject, ToInject> { ToInject("A", 1) })

        val first: IToInject by injection()
        val second: IToInject by injection()
        val third: IToInject by injection()

        assertNotNull(first)
        assertNotNull(second)
        assertNotNull(third)
        assertTrue { first !== second && second !== third }
    }

}