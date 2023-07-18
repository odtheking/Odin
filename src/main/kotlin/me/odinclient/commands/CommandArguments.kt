package me.odinclient.commands

class CommandArguments(private val data: Array<out String>): AbstractList<String>(), RandomAccess {

    override val size = data.size

    override fun get(index: Int): String {
        return data[index].lowercase()
    }

    fun getRegular(index: Int): String {
        return data[index]
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