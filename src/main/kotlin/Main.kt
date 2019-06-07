object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val avl = LsmTree(2)

        while (true) {
            val line = readLine() ?: ""
            val lArgs = line.split(' ')
            when (lArgs[0].trim()) {
                "ins" -> avl.insert(Integer.parseInt(lArgs[1].trim()), lArgs[2].trim())
                "rem" -> avl.remove(Integer.parseInt(lArgs[1].trim()))
                "print" -> avl.printRoot()
                "find" -> print((avl.getNode(Integer.parseInt(lArgs[1].trim()))) +  "\n")
            }
        }
    }
}