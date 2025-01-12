package com.sztorm.libtest2

import kotlin.test.Test

class Test {
    @Test
    fun testAll() {
        val functions = listOf(
            ::fun1,
            ::fun2,
            ::fun3,
            ::fun4,
            ::fun5,
            ::fun6,
            ::fun7,
            ::fun8,
            ::fun9,
            //::fun10,
        )
        val example = ExampleClass(2, 3f)
        val example2 = ExampleClass2(4, 5f)
        val example3 = functions.sumOf {
            it(2.0)
        }
        println(example)
        println(example2)
        println(example3)
        assert(true)
    }

    @Test
    fun test2() {
        assert(true)
    }
}