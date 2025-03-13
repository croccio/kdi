package it.croccio.kdi

import it.croccio.kdi.injectable.Bind
import it.croccio.kdi.injectable.Provide
import it.croccio.kdi.injectable.Singleton
import it.croccio.ticketshare.library.kdi.Module
import it.croccio.ticketshare.library.kdi.byInjection
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

    @Test
    fun `WHEN a function's parameter is injected THEN the inject value will be used in body function`() {

        inject(Singleton<ToInject> { ToInject("A", 1) })

        fun test(toInject: ToInject = byInjection()) {
            val first: ToInject by injection()

            assertNotNull(toInject)
            assertNotNull(first)
            assertTrue { first === toInject }
        }

        test()
    }

    @Test
    fun `WHEN a class's parameter is injected THEN the inject value will be used in body function`() {

        inject(Singleton<ToInject> { ToInject("A", 1) })

        class TestClass(
            val toInject: ToInject = byInjection()
        )

        val testClass = TestClass()
        val first: ToInject by injection()

        assertNotNull(testClass.toInject)
        assertNotNull(first)
        assertTrue { first === testClass.toInject }

    }

    @Test
    fun `WHEN a module is injected THEN we can use its injections`() {

        val module = object : Module {
            override fun register() {
                inject(Singleton<ToInject> { ToInject("A", 1) })
            }
        }

        inject(module)

        val first: ToInject by injection()
        val second: ToInject by injection()
        val third: ToInject by injection()

        assertNotNull(first)
        assertNotNull(second)
        assertNotNull(third)
        assertTrue { first === second && second === third }

    }

}