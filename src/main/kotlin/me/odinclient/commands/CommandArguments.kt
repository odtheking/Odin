package me.odinclient.commands

@Deprecated("No use.")
class CommandArguments(private val args: Array<out String>): AbstractList<String>(), RandomAccess {

    override val size = args.size

    override fun get(index: Int): String {
        return args[index].lowercase()
    }

    fun getRegular(index: Int): String {
        return args[index]
    }

    fun joinToString(startIndex: Int, endIndex: Int = this.size): String {
        if (startIndex < 0 || startIndex >= size || endIndex <= startIndex || endIndex > size) throw IndexOutOfBoundsException("Invalid range for arguments.")

        val result = StringBuilder()
        for (i in startIndex until endIndex) {
            if (i > startIndex) result.append(" ")
            result.append(this.getRegular(i))
        }
        return result.toString()
    }
}