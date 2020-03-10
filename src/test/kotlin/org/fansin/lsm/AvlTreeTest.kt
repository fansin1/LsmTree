package org.fansin.lsm

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AvlTreeTest {

    lateinit var avlTree: AvlTree<Int, String>

    @BeforeEach
    fun setUp() {
        avlTree = AvlTree<Int, String>(Comparator { o1, o2 -> o1.minus(o2) })
    }

    @Test
    fun insertTest() {
        for (i in 0..10) {
            avlTree.insert(i, "$i")
        }

        for (i in avlTree) {
            assertEquals("${i.key}", i.value)
        }

        assertEquals(4, avlTree.height())

    }

    @Test
    fun fullDeleteTest() {
        for (i in 0..10) {
            avlTree.insert(i, "$i")
        }

        for (i in 0..10) {
            avlTree.delete(i)
        }

        assertEquals(0, avlTree.height())
    }

    @Test
    fun partDeleteTest() {
        for (i in 0..10) {
            avlTree.insert(i, "$i")
        }

        for (i in 0..5) {
            avlTree.delete(i)
        }

        for (i in 6..10) {
            assertNotNull(avlTree.find(i))
        }

        assertEquals(2, avlTree.height())
    }

    @Test
    fun testSort() {
        for (i in 0..20) {
            avlTree.insert(i, "$i")
        }

        var prev = -1

        for (i in avlTree) {
            assertTrue(i.key > prev)
            prev = i.key
        }
    }

    @Test
    fun updateTest() {
        for (i in 0..10) {
            avlTree.insert(i, "$i")
        }

        for (i in 0..10) {
            avlTree.update(i, "${i * 2}")
        }
    }

    @Test
    fun toStringTest() {
        for (i in 0..10) {
            avlTree.insert(i, "$i")
        }

        val avlPrinted = avlTree.toString()

        val shouldBe = " ── 3\n" +
                "    ├── 1\n" +
                "    │   ├── 0\n" +
                "    │   └── 2\n" +
                "    └── 7\n" +
                "        ├── 5\n" +
                "        │   ├── 4\n" +
                "        │   └── 6\n" +
                "        └── 9\n" +
                "            ├── 8\n" +
                "            └── 10\n"

        assertEquals(shouldBe, avlPrinted)

    }

}