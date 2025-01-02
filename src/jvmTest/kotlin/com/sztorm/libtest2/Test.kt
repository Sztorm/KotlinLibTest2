package com.sztorm.libtest2

import kotlin.test.Test

class Test {
    @Test
    fun testAll() {
        val example = ExampleClass(2, 3f)
        val example2 = ExampleClass2(4, 5f)
        val example3 = exampleFunction(1f)
        //val example4 = exampleFunction2(2f)

        println(example)
        println(example2)
        println(example3)
        //println(example4)
        assert(true)
    }
}